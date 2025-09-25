package io.quarkiverse.observability.minecraft.runtime.ai;

import static java.time.Duration.ofSeconds;

import java.util.Optional;

import jakarta.inject.Singleton;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import io.quarkiverse.observability.minecraft.runtime.EntityMaker;
import io.quarkiverse.observability.minecraft.runtime.MinecrafterConfig;

@Singleton
public class LLMWuffMaker implements EntityMaker {
    static ChatModel model;
    private final MinecrafterConfig.Model config;
    EntityMaker maker;

    public LLMWuffMaker(MinecrafterConfig allConfig) {
        this.config = allConfig.model();
    }

    @Override
    public WuffStuff createWuff(String input) {
        try {
            WuffStuff wuff = getMaker().createWuff(input);
            System.out.println("LLM generated " + wuff);
            return wuff;
        } catch (Exception ee) {
            ee.printStackTrace();
            return new WuffStuff("unparseable", "#444", Pattern.PLAIN, false, 0);
        }

    }

    private EntityMaker getMaker() {
        if (model == null) {
            Optional<String> key = config.APIKey();

            OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                    .baseUrl(config.baseURL().get())
                    .modelName(config.name())
                    .temperature(1.0)
                    .timeout(ofSeconds(60));

            if (key.isPresent()) {
                builder.apiKey(key.get());
            }

            model = builder
                    .build();
        }
        if (maker == null) {
            maker = AiServices.create(EntityMaker.class, model);
        }
        return maker;
    }
}
