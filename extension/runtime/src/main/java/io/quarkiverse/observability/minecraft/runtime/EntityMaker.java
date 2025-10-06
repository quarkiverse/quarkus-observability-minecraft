package io.quarkiverse.observability.minecraft.runtime;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.observability.minecraft.runtime.ai.WuffStuff;

// Because this is an extension, we can't do the normal Quarkus @RegisterAiService goodness, and we can't register the interface as a CDI bean
public interface EntityMaker {

    @SystemMessage("Inside every application there are two wolves. Invent a fantastical, fantasy wolf, which is not like any of the other wolves.")
    @UserMessage("The theme of the wolf should be {{input}}.")
    public WuffStuff createWuff(String input);
}
