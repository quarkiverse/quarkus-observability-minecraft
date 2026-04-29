package io.quarkiverse.observability.minecraft.runtime;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;

@Singleton
public class MinecraftService {

    private final MinecrafterConfig minecrafterConfig;
    private final Client client;
    private final boolean useLLM;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private EntityMaker entityMaker;

    private Multi<String> healthStream;
    private volatile MultiEmitter<? super String> healthEmitter;
    private final HttpClient probeClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private volatile boolean serverAvailable = true;

    public MinecraftService(MinecrafterConfig minecrafterConfig, EntityMaker entityMaker) {
        this.minecrafterConfig = minecrafterConfig;
        this.entityMaker = entityMaker;
        this.client = ClientBuilder.newClient();
        this.useLLM = minecrafterConfig.model().baseURL().isPresent();
    }

    @PostConstruct
    void init() {
        healthStream = Multi.createFrom().<String> emitter(emitter -> {
            healthEmitter = emitter;
        }).broadcast().toAllSubscribers();

        Multi.createFrom().ticks().every(Duration.ofSeconds(2)).subscribe().with((item) -> {
            if (healthEmitter != null) {
                healthEmitter.emit(fetchPlayerHealth());
            }
        });
    }

    public void recordVisit(String params) {
        if (useLLM) {
            invokeMinecraft("event-with-details", entityMaker.createWuff(params));
        } else {
            invokeMinecraft("event");
        }
    }

    public void boom() {
        invokeMinecraft("boom");
    }

    public String setRespawn() {
        String response = invokeMinecraftSynchronously("set-respawn",
                String.valueOf(minecrafterConfig.allowRespawnOverWater()));
        return response.isEmpty() ? "Failed to set respawn point" : response;
    }

    public String killPlayer() {
        invokeMinecraft("kill");
        return "Killing player to trigger respawn";
    }

    public String respawnPlayer() {
        invokeMinecraft("respawn");
        return "Respawning player at new location";
    }

    public String killAndRespawn() {
        invokeMinecraft("kill-and-respawn");
        return "Killing and respawning player";
    }

    private String fetchPlayerHealth() {
        if (!serverAvailable) {
            if (!isServerReachable()) {
                return "?";
            }
            System.out.println("⛏️ [Minecrafter] Server connection restored");
            serverAvailable = true;
        }

        String healthStr = invokeMinecraftGet("health");
        if (healthStr.isEmpty()) {
            return "?";
        }
        try {
            int health = (int) Math.round(Double.parseDouble(healthStr));
            return String.valueOf(health);
        } catch (NumberFormatException e) {
            return healthStr;
        }
    }

    /**
     * Check if the Minecraft server is accepting HTTP requests, using
     * java.net.http.HttpClient to avoid Vert.x error logging on failure.
     * Any HTTP response (even 404) means the server is reachable.
     */
    private boolean isServerReachable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(minecrafterConfig.baseURL()))
                    .GET()
                    .timeout(Duration.ofSeconds(2))
                    .build();
            probeClient.send(request, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Multi<String> streamHealth() {
        return healthStream;
    }

    public void log(String message) {
        if (!serverAvailable) {
            return;
        }
        try {
            client.target(minecrafterConfig.baseURL())
                    .path("observability/log")
                    .request(MediaType.TEXT_PLAIN)
                    .post(Entity.text(message));
            // Don't log anything back about the response or it ends up with too much circular logging
        } catch (Throwable e) {
            System.out.println("\uD83D\uDDE1️ [Minecrafter] Connection error: " + e);
        }
    }

    private void invokeMinecraft(String path) {
        executor.submit(() -> invokeMinecraftSynchronously(path));
    }

    private void invokeMinecraft(String path, Object body) {
        executor.submit(() -> invokeMinecraftSynchronously(path, body));
    }

    private String invokeMinecraftGet(String path) {
        try {
            String response = client.target(minecrafterConfig.baseURL())
                    .path("observability/" + path)
                    .request(MediaType.TEXT_PLAIN)
                    .get()
                    .readEntity(String.class);

            if (!serverAvailable) {
                System.out.println("⛏️ [Minecrafter] Server connection restored");
                serverAvailable = true;
            }
            return response;
        } catch (Throwable e) {
            if (serverAvailable) {
                System.out.println("\uD83D\uDDE1️ [Minecrafter] Connection error: " + e);
            }
            serverAvailable = false;
            return "";
        }
    }

    private String invokeMinecraftSynchronously(String path) {
        return invokeMinecraftSynchronously(path, minecrafterConfig.animalType());
    }

    private String invokeMinecraftSynchronously(String path, Object body) {
        try {
            String response = client.target(minecrafterConfig.baseURL())
                    .path("observability/" + path)
                    .request(MediaType.TEXT_PLAIN)
                    .post(Entity.json(body))
                    .readEntity(String.class);

            System.out.println("\uD83D\uDDE1️ [Minecrafter] Mod response: " + response);
            return response;
        } catch (Throwable e) {
            System.out.println("\uD83D\uDDE1️ [Minecrafter] Connection error: " + e);
            return "";
        }
    }

}
