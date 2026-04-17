package io.quarkiverse.observability.minecraft.mod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityTypesInit {
    //    private static final DeferredRegister<EntityType<?>> BLOCKS = DeferredRegister.create(ENTITY_TYPES, MOD_ID);

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,
            QuarkiverseMod.MOD_ID);

    public static final RegistryObject<EntityType<Wuff>> CRAB_ENTITY = ENTITY_TYPES.register("crab",
            () -> createStandardEntityType("crab", Wuff::new, MobCategory.CREATURE, 1.3f, 1.8f));

    //    public static final RegistryObject<EntityType> EXAMPLE_BLOCK = BLOCKS.register("example_monster",
    //            () -> {
    //                CUSTOM_ENTITY_TYPE = EntityType.Builder.of(CustomEntity::new, MobCategory.CREATURE)
    //                        .sized(1.0F, 2.0F)
    //                        .fireImmune()
    //                        .updateInterval(1)
    //                        .build("quarkiversemod:example_monster");
    //                return CUSTOM_ENTITY_TYPE;
    //            });

    private static <T extends Entity> EntityType<T> createStandardEntityType(String entity_name,
            EntityType.EntityFactory<T> factory, MobCategory classification, float width,
            float height) {
        return EntityType.Builder.of(factory, classification)
                .sized(width, height)
                .build(QuarkiverseMod.MOD_ID + ":" + entity_name);
    }
}
