package com.example.examplemod;

import static com.example.examplemod.EntityTypesInit.CRAB_ENTITY;

import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("examplemod")
public class ExampleMod {
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    static EntityType CUSTOM_ENTITY_TYPE;

    static final String MOD_ID = "examplemod";

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, ExampleMod.MOD_ID);

    public ExampleMod() {
        // Register the setup method for modloading
        IEventBus bus = FMLJavaModLoadingContext.get()
                .getModEventBus();

        // Register ourselves for server and other game events we are interested in
        // Equivalent to MinecraftForge.EVENT_BUS.register(this), but honours SubscribeEvent annotations
        // Register two different kinds of @SubscribeEvent listeners on the two different bus accessors 
        bus.register(this);
        MinecraftForge.EVENT_BUS.register(new EventListener());

        EntityTypesInit.ENTITY_TYPES.register(bus);

        DistExecutor.safeCallWhenOn(Dist.CLIENT,
                () -> ClientSetup::adjustClient);

    }

    @SubscribeEvent
    public void newEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(CRAB_ENTITY.get(), Wuff.createExampleAttributes().build());
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    @SubscribeEvent
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CRAB_ENTITY.get(), WuffRenderer::new);
    }

    @SubscribeEvent
    public void processIMC(final InterModProcessEvent event) {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().map(m -> m.messageSupplier()
                .get()).collect(Collectors.toList()));
    }

    @SubscribeEvent
    public void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS,
                helper -> helper.register(new ResourceLocation(MOD_ID, "wuff"), Wuff.WUFF_DATA_SERIALIZER));
    }

    // This should not be needed if deferred registration is done, but removing it causes the wuffs to not appear
    @SubscribeEvent
    public static <T> void onRegisterEntityDataSerializers(RegisterEvent event) {
        event.getForgeRegistry().register("some-key", Wuff.WUFF_DATA_SERIALIZER);

    }

    // There are two methods for registering listener classes, and the event types
    // seem to differ between the two. 
    public static class EventListener {
        private static final Logger LOGGER = LogUtils.getLogger();

        // You can use SubscribeEvent and let the Event Bus discover methods to call
        @SubscribeEvent
        public void onServerStarting(ServerStartingEvent event) {
            // Do something when the server starts
            LOGGER.info("HELLO from server starting");

            ClassLoader cl = ClassLoader.getSystemClassLoader();

            new Listener().start();
        }

        @SubscribeEvent
        public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            System.out.println("QUARKCRAFT - Client connected: " + player);
            player.displayClientMessage(Component.literal("Hello from the Quarkiverse!"), true);

            PlayerWrapper playerWrapper = new PlayerWrapper(player);

            Endpoint.setPlayer(playerWrapper);

        }

    }
}
