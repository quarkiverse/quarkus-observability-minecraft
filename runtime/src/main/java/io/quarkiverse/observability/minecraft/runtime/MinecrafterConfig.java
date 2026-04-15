package io.quarkiverse.observability.minecraft.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.minecrafter")
public interface MinecrafterConfig {

    /**
     * The minecraft server's observability base URL
     */
    @WithDefault("http://localhost:8081/")
    String baseURL();

    /**
     * The kind of animal we spawn
     */
    @WithDefault("chicken")
    String animalType();

    /**
     * Whether to allow respawn points over water.
     */
    @WithDefault("false")
    boolean allowRespawnOverWater();

    /**
     * Details of the LLM used for generating creatures.
     */
    Model model();

    interface Model {
        /**
         * The API key for the model
         * Setting this using environment variables is a good practice.
         *
         */
        @WithDefault("")
        Optional<String> APIKey();

        /**
         * The base url for the model
         * For example, for Ollama, http://localhost:11434/v1/, or https://api.openai.com/v1/ for OpenAI
         */
        @WithDefault("")
        Optional<String> baseURL();

        /**
         * The name of the model
         */
        @WithDefault("llama3.2:3b")
        String name();
    }

}
