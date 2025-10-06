package io.quarkiverse.observability.minecraft.runtime.ai;

import dev.langchain4j.model.output.structured.Description;

public record WuffStuff(@Description("a poetic and thrilling name, at least a few words long") String name,
        @Description("a colour in rgb hex format, for example #6543AA") String colour,
        @Description("what the coat looks like. it should only be selected from the provided list") Pattern pattern,
        boolean isSitting,
        @Description("The angle of tilt of the head. Should be a value between -0.5 and 0.5, for example 0.1") float headRollAngle) {
}
