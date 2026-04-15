package io.quarkiverse.observability.minecraft.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "quarkus.minecrafter")
public interface MinecrafterDevServicesConfig {

    /**
     * Dev Services configuration.
     */
    DevServices devservices();

    interface DevServices {
        /**
         * Fixed host port to expose the Minecraft game server on.
         * If not set, an ephemeral host port is used.
         */
        Optional<Integer> port();
    }
}

// Made with Bob
