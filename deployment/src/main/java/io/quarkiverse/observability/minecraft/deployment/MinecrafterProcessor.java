package io.quarkiverse.observability.minecraft.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.util.Map;

import jakarta.ws.rs.Priorities;

import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.quarkiverse.observability.minecraft.runtime.*;
import io.quarkiverse.observability.minecraft.runtime.ai.LLMWuffMaker;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LogHandlerBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardAction;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;
import io.quarkus.runtime.configuration.ConfigUtils;

class MinecrafterProcessor {

    private static final Logger log = Logger.getLogger(MinecrafterProcessor.class);

    private static final String FEATURE = "minecrafter";
    private static final String BASE_URL_CONFIG_KEY = "quarkus.minecrafter.base-url";
    private static final DotName JAX_RS_GET = DotName.createSimple("jakarta.ws.rs.GET");
    private static final DotName JAX_RS_POST = DotName.createSimple("jakarta.ws.rs.POST");
    private static final DotName JAX_RS_PUT = DotName.createSimple("jakarta.ws.rs.PUT");

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> producer) {
        // RetryUtils has a static Random field that GraalVM disallows in the image heap
        producer.produce(new RuntimeInitializedClassBuildItem("dev.langchain4j.internal.RetryUtils"));
    }

    @Record(STATIC_INIT)
    @BuildStep
    public void helloBuildStep(HelloRecorder recorder) {
        recorder.sayHello("World");
    }

    @Record(RUNTIME_INIT)
    @BuildStep
    LogHandlerBuildItem addLogHandler(final MinecraftLogHandlerMaker maker, BeanContainerBuildItem beanContainer) {
        return new LogHandlerBuildItem(maker.create(beanContainer.getValue()));
    }

    /**
     * Makes the interceptor as a bean so we can access it.
     */
    @BuildStep
    void beans(BuildProducer<AdditionalBeanBuildItem> producer) {
        producer.produce(AdditionalBeanBuildItem.unremovableOf(MinecraftLogInterceptor.class));
        producer.produce(AdditionalBeanBuildItem.unremovableOf(MinecraftService.class));
        producer.produce(AdditionalBeanBuildItem.unremovableOf(LLMWuffMaker.class));

    }

    @BuildStep
    AnnotationsTransformerBuildItem transform() {
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {

            public boolean appliesTo(org.jboss.jandex.AnnotationTarget.Kind kind) {
                return kind == org.jboss.jandex.AnnotationTarget.Kind.METHOD;
            }

            public void transform(TransformationContext context) {
                if (context.getTarget()
                        .asMethod()
                        .hasAnnotation(JAX_RS_GET)
                        || context.getTarget()
                                .asMethod()
                                .hasAnnotation(JAX_RS_POST)
                        || context.getTarget()
                                .asMethod()
                                .hasAnnotation(JAX_RS_PUT)) {
                    context.transform()
                            .add(MinecraftLog.class)
                            .done();
                }
            }
        });
    }

    @BuildStep(onlyIf = IsLocalDevelopment.class)
    void createMinecraftPageOnCard(BuildProducer<CardPageBuildItem> cardsProducer) {

        CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();

        cardPageBuildItem.addPage(Page.webComponentPageBuilder()
                .title("Health")
                .icon("font-awesome-solid:heart")
                .streamingLabelJsonRPCMethodName("streamHealth"));

        cardPageBuildItem.addAction(CardAction.actionBuilder()
                .title("Reset Spawn Point")
                .icon("font-awesome-solid:bed")
                .jsonRpcMethodName("setRespawn")
                .build());
        cardPageBuildItem.addAction(CardAction.actionBuilder()
                .title("Kill and Respawn")
                .icon("font-awesome-solid:skull-crossbones")
                .jsonRpcMethodName("killAndRespawn")
                .build());

        cardPageBuildItem.setLogo("dark-chicken.png", "chicken.png");

        cardsProducer.produce(cardPageBuildItem);
    }

    @BuildStep
    ExceptionMapperBuildItem exceptionMappers() {
        return new ExceptionMapperBuildItem(RestExceptionMapper.class.getName(),
                Exception.class.getName(), Priorities.USER + 100, true);
    }

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = DevServicesConfig.Enabled.class)
    public DevServicesResultBuildItem createContainer(MinecrafterDevServicesConfig config) {

        MinecrafterDevServicesConfig.DevServices devservicesConfig = config.devservices();

        if (!devservicesConfig.enabled()) {
            log.debug("Not starting Dev Services for Minecraft as it has been disabled in the config");
            return null;
        }

        if (ConfigUtils.isPropertyNonEmpty(BASE_URL_CONFIG_KEY)) {
            log.debug("Not starting Dev Services for Minecraft as a base URL has been provided");
            return null;
        }

        final int minecraftApiPort = 8081;

        return DevServicesResultBuildItem.owned()
                .feature(FEATURE)
                .startable(() -> new MinecraftContainer(minecraftApiPort, devservicesConfig.port(),
                        devservicesConfig.reuse()))
                .configProvider(Map.of(BASE_URL_CONFIG_KEY,
                        c -> "http://" + c.getHost() + ":" + c.getMappedPort(minecraftApiPort)))
                .build();

    }

    @BuildStep(onlyIf = IsDevelopment.class)
    JsonRPCProvidersBuildItem createJsonRPCService() {
        return new JsonRPCProvidersBuildItem(MinecraftService.class);
    }
}
