package io.quarkiverse.observability.minecraft.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Pure-math helpers for computing spawn locations.
 */
final class SpawnLocationHelper {

    record DistanceRange(int min, int max) {
    }

    private SpawnLocationHelper() {
    }

    /**
     * Compute the min and max respawn distance from the world diameter.
     */
    static DistanceRange computeRespawnDistanceRange(double worldDiameter) {
        int maxDistance = Math.max(1, (int) (worldDiameter / 2.0 * 0.7));
        int minDistance = Math.min(30, maxDistance);
        return new DistanceRange(minDistance, maxDistance);
    }

    /**
     * Compute a target XZ block position offset from the player by the given angle
     * and distance, clamped to stay within the max world radius.
     * The returned BlockPos has y=0.
     */
    static BlockPos computeTargetPosition(double playerX, double playerZ, double angle, int distance,
            int maxWorldRadius) {
        int targetX = (int) (playerX + Math.cos(angle) * distance);
        int targetZ = (int) (playerZ + Math.sin(angle) * distance);
        targetX = Math.max(-maxWorldRadius + 1, Math.min(maxWorldRadius - 1, targetX));
        targetZ = Math.max(-maxWorldRadius + 1, Math.min(maxWorldRadius - 1, targetZ));
        return new BlockPos(targetX, 0, targetZ);
    }

    /**
     * Compute a position in front of the player for spawning entities (chickens,
     * frogs, etc.). X and Z are offset along the player's look direction; Y is
     * raised by {@code distance} blocks above the player.
     */
    static Vec3 computePositionInFrontOf(double playerX, double playerY, double playerZ,
            double lookAngleX, double lookAngleZ, int distance) {
        double x = playerX + distance * lookAngleX;
        double y = playerY + distance;
        double z = playerZ + distance * lookAngleZ;
        return new Vec3(x, y, z);
    }
}
