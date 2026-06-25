package io.quarkiverse.observability.minecraft.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "quarkus.minecrafter")
public interface MinecrafterDevServicesConfig {

    /**
     * Dev Services configuration.
     */
    DevServices devservices();

    interface DevServices {
        /**
         * Whether the Minecraft dev service is enabled.
         * Dev Services is generally enabled by default, unless an existing
         * {@code quarkus.minecrafter.base-url} is configured.
         */
        @WithDefault("true")
        boolean enabled();

        /**
         * Fixed host port to expose the Minecraft game server on.
         * If not set, an ephemeral host port is used.
         */
        Optional<Integer> port();

        /**
         * Whether to reuse the container across test runs.
         * If true, the container will be kept running after tests complete.
         * Defaults to true for faster test iterations during development.
         */
        @WithDefault("true")
        boolean reuse();
    }
}