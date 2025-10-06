package com.example.examplemod;

import static com.example.examplemod.EntityTypesInit.CRAB_ENTITY;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.mojang.authlib.minecraft.client.ObjectMapper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * We need a wrapper here because we need something with a simple enough
 * signature that everything in it is in a JVM library, rather than loaded
 * by one of the fragmented classloaders. That allows us to find and
 * invoke the method by reflection.
 */
public class PlayerWrapper {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private final Player player;
    ObjectMapper objectMapper;

    PlayerWrapper(Player player) {
        this.player = player;
        objectMapper = new ObjectMapper(new Gson());

    }

    private static void makeLightning(Level world, Vec3 pos) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world);
        lightning.setPos(pos);
        lightning.setVisualOnly(true);
        world.addFreshEntity(lightning);
    }

    // Called reflectively
    public void event(String message, String animalName) {

        Vec3 pos = getPositionInFrontOfPlayer(3);

        player.displayClientMessage(Component.literal(message), true);

        Level world = player.getCommandSenderWorld();

        makeLightning(world, pos);

        Entity animal = getAnimalType(animalName).create(world);
        animal.setPos(pos);
        String time = DATE_FORMAT.format(new Date());
        Component timeComponent = Component.literal(time);
        animal.setCustomName(timeComponent);
        animal.setCustomNameVisible(true);
        world.addFreshEntity(animal);
    }

    // Called reflectively
    public void customEvent(String message, String animalJson) {

        Vec3 pos = getPositionInFrontOfPlayer(3);

        player.displayClientMessage(Component.literal(message), true);

        Level world = player.getCommandSenderWorld();

        makeLightning(world, pos);

        WuffStuff wuffStuff = objectMapper.readValue(animalJson, WuffStuff.class);
        Wuff animal = CRAB_ENTITY.get().create(world);
        animal.setPos(pos);
        animal.setWuffStuff(wuffStuff);
        Component nameTag = Component.literal(wuffStuff.getName());
        animal.setCustomName(nameTag);
        animal.setCustomNameVisible(true);
        world.addFreshEntity(animal);

    }

    private EntityType getAnimalType(String animalName) {
        EntityType animalType;
        switch (animalName) {
            case "cat":
                animalType = EntityType.CAT;
                break;
            case "cow":
                animalType = EntityType.COW;
                break;
            case "horse":
                animalType = EntityType.HORSE;
                break;
            case "pig":
                animalType = EntityType.PIG;
                break;
            case "rabbit":
                animalType = EntityType.RABBIT;
                break;
            case "sheep":
                animalType = EntityType.SHEEP;
                break;
            case "chicken":
                animalType = EntityType.CHICKEN;
                break;
            default:
                animalType = EntityType.CHICKEN;
                break;
        }
        return animalType;
    }

    public void say(String message, String ignored) {
        // Use the chat interface for logs since it wraps more nicely
        Component msg = Component.literal(message);
        player.sendSystemMessage(msg);
    }

    public void explode(String message, String ignored) {
        player.displayClientMessage(Component.literal(message), true);
        Level level = player.getCommandSenderWorld();

        Entity animal = EntityType.FROG.create(level);
        animal.setPos(getPositionInFrontOfPlayer(6));
        level.addFreshEntity(animal);

        List<BlockPos> affectedPositions = new ArrayList();
        affectedPositions.add(new BlockPos(animal.getX(), animal.getY(),
                animal.getZ()));
        Explosion explosion = new Explosion(level, animal, animal.getX(), animal.getY(),
                animal.getZ(), 6F, affectedPositions);

        explosion.explode();
    }

    @NotNull
    private Vec3 getPositionInFrontOfPlayer(int distance) {
        double x = player.getX() + distance * player.getLookAngle().x;
        double y = player.getY() + distance; // Spawn at the same height as the player, but a bit up in the air
        double z = player.getZ() + distance * player.getLookAngle().z;

        Vec3 pos = new Vec3(x, y, z);
        return pos;
    }

}
