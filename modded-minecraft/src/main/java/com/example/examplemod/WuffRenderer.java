package com.example.examplemod;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WuffRenderer extends MobRenderer<Wuff, WuffModel<Wuff>> {

    protected static final ResourceLocation WOLF_LOCATION = new ResourceLocation(ExampleMod.MOD_ID,
            "textures/entity/wuff.png");
    protected static final ResourceLocation WOLF_LOCATION_WHITE_SPOT = new ResourceLocation(ExampleMod.MOD_ID,
            "textures/entity/wuff-white-spot.png");
    protected static final ResourceLocation WOLF_LOCATION_BLACK_SPOT = new ResourceLocation(ExampleMod.MOD_ID,
            "textures/entity/wuff-black-spot.png");
    protected static final ResourceLocation WOLF_LOCATION_STRIPES = new ResourceLocation(ExampleMod.MOD_ID,
            "textures/entity/wuff-stripe.png");
    protected static final ResourceLocation WOLF_LOCATION_SPECTRAL_STRIPES = new ResourceLocation(ExampleMod.MOD_ID,
            "textures/entity/wuff-spectral.png");

    public WuffRenderer(EntityRendererProvider.Context p_174452_) {
        super(p_174452_, new WuffModel<>(p_174452_.bakeLayer(ModelLayers.WOLF)), 0.5F);
        //        this.addLayer(new WolfCollarLayer(this));
    }

    protected float getBob(Wuff wuff, float p_116529_) {
        return wuff.getTailAngle();
    }

    public void render(Wuff wuff, float p_116532_, float p_116533_, PoseStack p_116534_, MultiBufferSource p_116535_,
            int p_116536_) {
        String f = wuff.getColour(p_116533_);
        Colour colour = new Colour(f);
        this.model.setColor(colour.getR(), colour.getG(), colour.getB());

        super.render(wuff, p_116532_, p_116533_, p_116534_, p_116535_, p_116536_);

        this.model.setColor(colour.getR(), colour.getG(), colour.getB());

    }

    @Override
    public ResourceLocation getTextureLocation(Wuff wuff) {
        switch (wuff.getPattern()) {
            case PLAIN -> {
                return WOLF_LOCATION;
            }
            case WHITE_SPOTS -> {
                return WOLF_LOCATION_WHITE_SPOT;
            }
            case BLACK_SPOTS -> {
                return WOLF_LOCATION_BLACK_SPOT;
            }
            case STRIPES -> {
                return WOLF_LOCATION_STRIPES;
            }
            case SPECTRAL_STRIPES -> {
                return WOLF_LOCATION_SPECTRAL_STRIPES;
            }
            default -> {
                return WOLF_LOCATION;
            }
        }

    }
}