package io.quarkiverse.observability.minecraft.deployment;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import io.quarkus.deployment.builditem.Startable;

public class MinecraftContainer extends GenericContainer<MinecraftContainer> implements Startable {
    final int minecraftGamePort = 25565;

    // Normally, this would be a remote image, but we need to build one with the right mods, so use a local one
    //    private static final DockerImageName dockerImageName = DockerImageName.parse();
    private final int minecraftApiPort;

    public MinecraftContainer(final int minecraftApiPort) {
        // This will be cached, so it won't be disastrously slow, but it may not update
        super(new ImageFromDockerfile("minecraft-server")
                .withDockerfile(Path.of("../modded-minecraft/Dockerfile")));
        this.minecraftApiPort = minecraftApiPort;
        this.waitingFor(Wait.forLogMessage(".*" + "Preparing" + ".*", 1))
                .withReuse(true)
                .withExposedPorts(minecraftApiPort, minecraftGamePort);

        // Make life easy for the minecraft client by fixing the client port
        // This could be configurable
        List<String> portBindings = new ArrayList<>();
        portBindings.add(minecraftGamePort + ":" + minecraftGamePort);
        this.setPortBindings(portBindings);

    }

    @Override
    public String getConnectionInfo() {
        return "http://" + getHost() + ":" + getMappedPort(minecraftApiPort);
    }

    @Override
    public void close() {
        super.close();
    }
}
