package io.quarkiverse.observability.minecraft.runtime;

import java.util.Optional;
import java.util.logging.Handler;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MinecraftLogHandlerMaker {

    public RuntimeValue<Optional<Handler>> create(BeanContainer beanContainer) {
        MinecraftService minecraft = beanContainer.beanInstance(MinecraftService.class);
        Handler handler = new MinecraftLogHandler(minecraft);
        return new RuntimeValue<>(Optional.of(handler));

    }
}
