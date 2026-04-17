package io.quarkiverse.observability.minecraft.mod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class CustomEntity extends Mob {
    public CustomEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

}
