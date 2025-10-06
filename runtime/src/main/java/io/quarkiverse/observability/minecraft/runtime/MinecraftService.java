package io.quarkiverse.observability.minecraft.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.inject.Singleton;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import io.quarkiverse.observability.minecraft.runtime.ai.WuffStuff;

@Singleton
public class MinecraftService {

    private final MinecrafterConfig minecrafterConfig;
    private final Client client;
    private final boolean useLLM;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private EntityMaker entityMaker;

    public MinecraftService(MinecrafterConfig minecrafterConfig, EntityMaker entityMaker) {
        this.minecrafterConfig = minecrafterConfig;
        this.entityMaker = entityMaker;
        this.client = ClientBuilder.newClient();
        this.useLLM = minecrafterConfig.model().baseURL().isPresent();
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

    public void log(String message) {
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

    private void invokeMinecraft(String path, WuffStuff wuffStuff) {
        executor.submit(() -> invokeMinecraftSynchronously(path, wuffStuff));
    }

    private void invokeMinecraftSynchronously(String path) {
        try {
            String response = client.target(minecrafterConfig.baseURL())
                    .path("observability/" + path)
                    .request(MediaType.TEXT_PLAIN)
                    .post(Entity.text(minecrafterConfig.animalType()))
                    .readEntity(String.class);

            System.out.println("\uD83D\uDDE1️ [Minecrafter] Mod response: " + response);
        } catch (Throwable e) {
            System.out.println("\uD83D\uDDE1️ [Minecrafter] Connection error: " + e);
        }
    }

    private void invokeMinecraftSynchronously(String path, WuffStuff stuff) {
        try {
            String response = client.target(minecrafterConfig.baseURL())
                    .path("observability/" + path)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(stuff))
                    .readEntity(String.class);

            System.out.println("\uD83D\uDDE1️ [Minecrafter] Mod response: " + response);
        } catch (Throwable e) {
            System.out.println("\uD83D\uDDE1️ [Minecrafter] Connection error: " + e);
        }
    }

}
