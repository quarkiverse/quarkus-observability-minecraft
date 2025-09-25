package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;

public class Wuff extends Wolf {

    private static final EntityDataAccessor<Integer> DATA_DARK_TICKS_REMAINING = SynchedEntityData.defineId(Wuff.class,
            EntityDataSerializers.INT);

    public static final EntityDataSerializer<WuffStuff> WUFF_DATA_SERIALIZER = new EntityDataSerializer.ForValueType<>() {
        public void write(FriendlyByteBuf buf, WuffStuff wuffStuff) {
            buf.writeUtf(wuffStuff.getColour());
            buf.writeEnum(wuffStuff.getPattern());
            buf.writeBoolean(wuffStuff.isSitting());
        }

        public WuffStuff read(FriendlyByteBuf buf) {
            WuffStuff wuff = new WuffStuff();
            wuff.setColour(buf.readUtf());
            wuff.setPattern(buf.readEnum(Pattern.class));
            wuff.setSitting(buf.readBoolean());
            return wuff;
        }
    };

    private static final EntityDataAccessor<WuffStuff> WUFF_DATA = SynchedEntityData.defineId(Wuff.class,
            WUFF_DATA_SERIALIZER);

    protected Wuff(EntityType<? extends Wolf> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);

    }

    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DARK_TICKS_REMAINING, 0);

        this.entityData.define(WUFF_DATA, new WuffStuff());// Register with an initial value
    }

    public static AttributeSupplier.Builder createExampleAttributes() {
        return Mob.createMobAttributes().add(Attributes.KNOCKBACK_RESISTANCE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ARMOR, 0.35D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 0.35D)
                .add(Attributes.MAX_HEALTH, 10.0D);
    }

    public boolean isWet() {
        return true;
    }

    public String getColour(float p116533) {
        return this.entityData.get(WUFF_DATA).getColour();
    }

    public boolean isTame() {
        return false;
    }

    public boolean isAngry() {
        return false;
    }

    public float getHeadRollAngle(float p104135) {
        return getWuffStuff().getHeadRollAngle();
    }

    public float getBodyRollAngle(float p104135, float v) {
        return 0.05f;
    }

    public boolean isInSittingPose() {
        return getWuffStuff().isSitting();
    }

    private WuffStuff getWuffStuff() {
        return this.entityData.get(WUFF_DATA);
    }

    public void setWuffStuff(WuffStuff wuffStuff) {
        this.getEntityData().set(WUFF_DATA, wuffStuff);
    }

    public Pattern getPattern() {
        return getWuffStuff().getPattern();
    }
}
