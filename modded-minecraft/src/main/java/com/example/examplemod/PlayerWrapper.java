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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
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

    private static void makeLightning(ServerLevel world, Vec3 pos) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world);
        lightning.setPos(pos);
        lightning.setVisualOnly(true);
        world.addFreshEntity(lightning);
    }

    // Called reflectively
    public void event(String message, String animalName) {
        player.displayClientMessage(Component.literal(message), true);

        Level world = player.getCommandSenderWorld();
        if (world instanceof ServerLevel serverLevel) {
            // Must run on the server thread — this method is called from an
            // Undertow HTTP thread, and addFreshEntity is not thread-safe.
            serverLevel.getServer().execute(() -> {
                Vec3 pos = getPositionInFrontOfPlayer(3);

                makeLightning(serverLevel, pos);

                Entity animal = getAnimalType(animalName).create(serverLevel);
                animal.setPos(pos);
                String time = DATE_FORMAT.format(new Date());
                animal.setCustomName(Component.literal(time));
                animal.setCustomNameVisible(true);
                serverLevel.addFreshEntity(animal);
            });
        }
    }

    // Called reflectively
    public void customEvent(String message, String animalJson) {
        player.displayClientMessage(Component.literal(message), true);

        Level world = player.getCommandSenderWorld();
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                Vec3 pos = getPositionInFrontOfPlayer(3);

                makeLightning(serverLevel, pos);

                WuffStuff wuffStuff = objectMapper.readValue(animalJson, WuffStuff.class);
                Wuff animal = CRAB_ENTITY.get().create(serverLevel);
                animal.setPos(pos);
                animal.setWuffStuff(wuffStuff);
                animal.setCustomName(Component.literal(wuffStuff.getName()));
                animal.setCustomNameVisible(true);
                serverLevel.addFreshEntity(animal);
            });
        }
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

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                Entity animal = EntityType.FROG.create(serverLevel);
                animal.setPos(getPositionInFrontOfPlayer(6));
                serverLevel.addFreshEntity(animal);

                List<BlockPos> affectedPositions = new ArrayList<>();
                affectedPositions.add(new BlockPos(animal.getX(), animal.getY(),
                        animal.getZ()));
                Explosion explosion = new Explosion(serverLevel, animal, animal.getX(), animal.getY(),
                        animal.getZ(), 6F, affectedPositions);

                explosion.explode();
            });
        }
    }

    public void setRespawn(String message, String ignored) {
        player.displayClientMessage(Component.literal(message), true);

        Level level = player.getLevel();
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            // Pick a random direction and go 300-500 blocks away
            double angle = Math.random() * 2 * Math.PI;
            int distance = 300 + (int) (Math.random() * 200);
            int targetX = (int) (player.getX() + Math.cos(angle) * distance);
            int targetZ = (int) (player.getZ() + Math.sin(angle) * distance);

            // Must run on the server thread — this method is called from an
            // Undertow HTTP thread, and setRespawnPosition must be visible to
            // the death/respawn processing that happens later.
            serverLevel.getServer().execute(() -> {
                // Force the target chunk to load so terrain is generated and
                // the heightmap is available — avoids crashes from unknown chunks
                serverLevel.getChunk(targetX >> 4, targetZ >> 4);

                // Find a safe surface Y using the heightmap, then scan upward
                // until we find two clear blocks for the player to stand in.
                // Minecraft's respawn logic rejects positions without headroom.
                int y = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, targetX, targetZ);
                while (y < serverLevel.getMaxBuildHeight() - 1
                        && (!serverLevel.getBlockState(new BlockPos(targetX, y, targetZ))
                                .getCollisionShape(serverLevel, new BlockPos(targetX, y, targetZ)).isEmpty()
                                || !serverLevel.getBlockState(new BlockPos(targetX, y + 1, targetZ))
                                        .getCollisionShape(serverLevel, new BlockPos(targetX, y + 1, targetZ)).isEmpty())) {
                    y++;
                }
                BlockPos spawnPosition = new BlockPos(targetX, y, targetZ);

                serverPlayer.setRespawnPosition(
                        serverLevel.dimension(),
                        spawnPosition,
                        0.0F,
                        true,
                        false);

                player.displayClientMessage(
                        Component.literal("Respawn set " + distance + " blocks away at: " + spawnPosition.toShortString()),
                        false);
            });
        }
    }

    public void killPlayer(String message, String ignored) {
        player.displayClientMessage(Component.literal(message), true);

        Level level = player.getLevel();
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            // Must run on the server thread — Minecraft ignores death
            // processing off-thread.
            serverLevel.getServer().execute(() -> {
                if (serverPlayer.isAlive()) {
                    serverPlayer.kill();
                }
            });
        }
    }

    public void respawnPlayer(String message, String ignored) {
        Level level = player.getLevel();
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            serverLevel.getServer().execute(() -> {
                if (!serverPlayer.isAlive()) {
                    PlayerList playerList = serverPlayer.server.getPlayerList();
                    ServerPlayer newPlayer = playerList.respawn(serverPlayer, false);
                    // respawn() creates a new ServerPlayer; update the reference
                    // so subsequent calls operate on the live player.
                    Endpoint.setPlayer(new PlayerWrapper(newPlayer));
                }
            });
        }
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
