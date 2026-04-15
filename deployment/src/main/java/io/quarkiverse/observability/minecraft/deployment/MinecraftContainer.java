package io.quarkiverse.observability.minecraft.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.builditem.Startable;

public class MinecraftContainer extends GenericContainer<MinecraftContainer> implements Startable {
    final int minecraftGamePort = 25565;

    // Normally, this would be a remote image, but we need to build one with the right mods, so use a local one
    private static final DockerImageName dockerImageName = DockerImageName.parse("minecraft-server");
    private final int minecraftApiPort;

    public MinecraftContainer(final int minecraftApiPort, Optional<Integer> devServicesPort) {
        super(dockerImageName);
        this.minecraftApiPort = minecraftApiPort;
        this.waitingFor(Wait.forLogMessage(".*" + "Preparing" + ".*", 1))
                .withReuse(true)
                .withExposedPorts(minecraftApiPort, minecraftGamePort);

        devServicesPort.ifPresent(port -> {
            List<String> portBindings = new ArrayList<>();
            portBindings.add(port + ":" + minecraftGamePort);
            this.setPortBindings(portBindings);
        });

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
