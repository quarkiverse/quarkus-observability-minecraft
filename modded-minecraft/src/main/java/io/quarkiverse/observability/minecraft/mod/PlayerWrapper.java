package io.quarkiverse.observability.minecraft.mod;

import static io.quarkiverse.observability.minecraft.mod.EntityTypesInit.CRAB_ENTITY;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private static final int MAX_RESPAWN_ATTEMPTS = 20;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private final Player player;
    ObjectMapper objectMapper;

    PlayerWrapper(Player player) {
        this.player = player;
        objectMapper = new ObjectMapper(new Gson());

    }

    /**
     * Dispatch a named method onto the server thread. Methods in this class are
     * called from Undertow HTTP threads via {@link Endpoint}, but most Minecraft
     * operations (spawning entities, killing players, etc.) are not thread-safe.
     * The Endpoint calls this single entry point, which handles both the
     * thread dispatch and the reflective method invocation.
     */
    public void invokeOnServerThread(String methodName, String message, String param) {
        Level level = player.getCommandSenderWorld();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                try {
                    Method m = getClass().getMethod(methodName, String.class, String.class);
                    m.invoke(this, message, param);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Returns the player's world as a {@link ServerLevel}.
     * Only safe to call from methods running on the server thread
     * (i.e. dispatched via {@link #invokeOnServerThread}).
     */
    private ServerLevel getServerLevel() {
        return (ServerLevel) player.getCommandSenderWorld();
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

        ServerLevel serverLevel = getServerLevel();
        Vec3 pos = getPositionInFrontOfPlayer(3);

        makeLightning(serverLevel, pos);

        Entity animal = getAnimalType(animalName).create(serverLevel);
        animal.setPos(pos);
        String time = DATE_FORMAT.format(new Date());
        animal.setCustomName(Component.literal(time));
        animal.setCustomNameVisible(true);
        serverLevel.addFreshEntity(animal);
    }

    // Called reflectively
    public void customEvent(String message, String animalJson) {
        player.displayClientMessage(Component.literal(message), true);

        ServerLevel serverLevel = getServerLevel();
        Vec3 pos = getPositionInFrontOfPlayer(3);

        makeLightning(serverLevel, pos);

        WuffStuff wuffStuff = objectMapper.readValue(animalJson, WuffStuff.class);
        Wuff animal = CRAB_ENTITY.get().create(serverLevel);
        animal.setPos(pos);
        animal.setWuffStuff(wuffStuff);
        animal.setCustomName(Component.literal(wuffStuff.getName()));
        animal.setCustomNameVisible(true);
        serverLevel.addFreshEntity(animal);
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

        ServerLevel serverLevel = getServerLevel();
        Entity animal = EntityType.FROG.create(serverLevel);
        animal.setPos(getPositionInFrontOfPlayer(6));
        serverLevel.addFreshEntity(animal);

        List<BlockPos> affectedPositions = new ArrayList<>();
        affectedPositions.add(new BlockPos(animal.getX(), animal.getY(),
                animal.getZ()));
        Explosion explosion = new Explosion(serverLevel, animal, animal.getX(), animal.getY(),
                animal.getZ(), 6F, affectedPositions);

        explosion.explode();
    }

    public void setRespawn(String message, String config) {
        boolean allowWater = Boolean.parseBoolean(config);
        player.displayClientMessage(Component.literal(message), true);

        ServerLevel serverLevel = getServerLevel();
        if (player instanceof ServerPlayer serverPlayer) {
            // Skip retries when water is acceptable
            int maxAttempts = allowWater ? 1 : MAX_RESPAWN_ATTEMPTS;
            BlockPos spawnPosition = null;
            int chosenDistance = 0;
            boolean isWater = false;

            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                // Pick a random direction, choosing a distance based on the max world size.
                // Note: getAbsoluteMaxWorldSize() returns the max-world-size server property
                // (a radius), NOT WorldBorder.getSize() which defaults to ~60M regardless.
                int maxWorldRadius = serverLevel.getServer().getAbsoluteMaxWorldSize();
                SpawnLocationHelper.DistanceRange range = SpawnLocationHelper.computeRespawnDistanceRange(maxWorldRadius * 2.0);
                double angle = Math.random() * 2 * Math.PI;
                int distance = range.min() + (int) (Math.random() * Math.max(1, range.max() - range.min()));
                BlockPos target = SpawnLocationHelper.computeTargetPosition(player.getX(), player.getZ(),
                        angle, distance, maxWorldRadius);

                // Force the target chunk to load so terrain is generated and
                // the heightmap is available — avoids crashes from unknown chunks
                serverLevel.getChunk(target.getX() >> 4, target.getZ() >> 4);

                // Find a safe surface Y using the heightmap, then scan upward
                // until we find two clear blocks for the player to stand in.
                // Minecraft's respawn logic rejects positions without headroom.
                int y = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, target.getX(), target.getZ());
                while (y < serverLevel.getMaxBuildHeight() - 1
                        && (!serverLevel.getBlockState(new BlockPos(target.getX(), y, target.getZ()))
                                .getCollisionShape(serverLevel, new BlockPos(target.getX(), y, target.getZ())).isEmpty()
                                || !serverLevel.getBlockState(new BlockPos(target.getX(), y + 1, target.getZ()))
                                        .getCollisionShape(serverLevel, new BlockPos(target.getX(), y + 1, target.getZ()))
                                        .isEmpty())) {
                    y++;
                }

                spawnPosition = new BlockPos(target.getX(), y, target.getZ());
                chosenDistance = distance;

                isWater = !serverLevel.getBlockState(new BlockPos(target.getX(), y - 1, target.getZ())).getFluidState()
                        .isEmpty();
                if (allowWater || !isWater) {
                    break;
                }
            }

            serverPlayer.setRespawnPosition(
                    serverLevel.dimension(),
                    spawnPosition,
                    0.0F,
                    true,
                    false);

            String biome = serverLevel.getBiome(spawnPosition).unwrapKey()
                    .map(key -> key.location().getPath())
                    .orElse("unknown");
            String warning = isWater
                    ? " (no dry land found after " + MAX_RESPAWN_ATTEMPTS + " attempts — respawn is over water)"
                    : "";
            player.displayClientMessage(
                    Component.literal(
                            "Respawn set " + chosenDistance + " blocks away in " + biome + " at: "
                                    + spawnPosition.toShortString() + warning),
                    false);
        }
    }

    public void killAndRespawn(String message, String ignored) {
        player.displayClientMessage(Component.literal(message), true);
        if (player instanceof ServerPlayer serverPlayer) {

            if (serverPlayer.isAlive()) {
                serverPlayer.kill();
            }
            if (!serverPlayer.isAlive()) {
                PlayerList playerList = serverPlayer.server.getPlayerList();
                ServerPlayer newPlayer = playerList.respawn(serverPlayer, false);
                Endpoint.setPlayer(new PlayerWrapper(newPlayer));
            }
        }
    }

    @NotNull
    private Vec3 getPositionInFrontOfPlayer(int distance) {
        return SpawnLocationHelper.computePositionInFrontOf(
                player.getX(), player.getY(), player.getZ(),
                player.getLookAngle().x, player.getLookAngle().z, distance);
    }

}
