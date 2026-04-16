package com.example.examplemod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

class SpawnLocationHelperTest {

    private static final int CONFIGURED_MAX_WORLD_RADIUS = 288;
    private static final double DEFAULT_WORLD_BORDER_SIZE = 59_999_968;

    // --- computeRespawnDistanceRange ---

    @Nested
    class RespawnDistanceRange {

        @Test
        void reasonableForConfiguredWorldSize() {
            SpawnLocationHelper.DistanceRange range = SpawnLocationHelper
                    .computeRespawnDistanceRange(CONFIGURED_MAX_WORLD_RADIUS * 2.0);

            // diameter 576: maxDistance = (int)(576 / 2 * 0.7) = 201
            assertTrue(range.max() <= 250,
                    "Max respawn distance should be <= 250, but was " + range.max());
            assertTrue(range.min() <= range.max(),
                    "Min distance should be <= max distance");
        }

        @Test
        void defaultWorldBorderProducesAbsurdDistances() {
            // Documents the bug: WorldBorder.getSize() (~60M) must not be used.
            SpawnLocationHelper.DistanceRange range = SpawnLocationHelper
                    .computeRespawnDistanceRange(DEFAULT_WORLD_BORDER_SIZE);

            assertTrue(range.max() > 1_000_000,
                    "With the default world border (~60M), max distance should be millions, "
                            + "demonstrating why the caller must use getAbsoluteMaxWorldSize(). "
                            + "Got: " + range.max());
        }

        @Test
        void minDistanceCapsAt30() {
            SpawnLocationHelper.DistanceRange range = SpawnLocationHelper.computeRespawnDistanceRange(1000);
            assertEquals(30, range.min());
        }

        @Test
        void verySmallWorldClampsMinToMax() {
            // diameter 20: maxDistance = (int)(20 / 2 * 0.7) = 7
            SpawnLocationHelper.DistanceRange range = SpawnLocationHelper.computeRespawnDistanceRange(20);
            assertEquals(7, range.min(), "minDistance should equal maxDistance for tiny worlds");
            assertEquals(7, range.max());
        }
    }

    // --- computeTargetPosition ---

    @Nested
    class TargetPosition {

        @Test
        void offsetFromOriginEast() {
            BlockPos pos = SpawnLocationHelper.computeTargetPosition(0, 0, 0, 100, CONFIGURED_MAX_WORLD_RADIUS);
            assertEquals(100, pos.getX(), "X should be offset by distance when angle is 0 (east)");
            assertEquals(0, pos.getZ(), "Z should be unchanged when angle is 0");
        }

        @Test
        void offsetFromOriginSouth() {
            BlockPos pos = SpawnLocationHelper.computeTargetPosition(0, 0, Math.PI / 2, 100,
                    CONFIGURED_MAX_WORLD_RADIUS);
            assertTrue(Math.abs(pos.getX()) <= 1, "X should be ~0 when moving south, but was " + pos.getX());
            assertEquals(100, pos.getZ(), "Z should be offset by distance when angle is PI/2 (south)");
        }

        @Test
        void offsetFromNonOriginPlayer() {
            BlockPos pos = SpawnLocationHelper.computeTargetPosition(50, 50, 0, 100, CONFIGURED_MAX_WORLD_RADIUS);
            assertEquals(150, pos.getX(), "X should be player + distance");
            assertEquals(50, pos.getZ(), "Z should stay at player Z");
        }

        @Test
        void clampedWhenPlayerNearPositiveEdge() {
            BlockPos pos = SpawnLocationHelper.computeTargetPosition(280, 0, 0, 100, CONFIGURED_MAX_WORLD_RADIUS);
            assertEquals(CONFIGURED_MAX_WORLD_RADIUS - 1, pos.getX(),
                    "X should be clamped to maxWorldRadius - 1");
        }

        @Test
        void clampedInNegativeDirection() {
            BlockPos pos = SpawnLocationHelper.computeTargetPosition(-280, 0, Math.PI, 100,
                    CONFIGURED_MAX_WORLD_RADIUS);
            assertEquals(-CONFIGURED_MAX_WORLD_RADIUS + 1, pos.getX(),
                    "X should be clamped to -maxWorldRadius + 1");
        }

        @Test
        void clampedOnBothAxes() {
            double angle = Math.PI / 4; // northeast
            BlockPos pos = SpawnLocationHelper.computeTargetPosition(280, 280, angle, 200,
                    CONFIGURED_MAX_WORLD_RADIUS);
            assertTrue(pos.getX() <= CONFIGURED_MAX_WORLD_RADIUS - 1,
                    "X should be clamped, but was " + pos.getX());
            assertTrue(pos.getZ() <= CONFIGURED_MAX_WORLD_RADIUS - 1,
                    "Z should be clamped, but was " + pos.getZ());
        }

        @Test
        void staysWithinBoundsForAllDirections() {
            for (int degrees = 0; degrees < 360; degrees += 15) {
                double angle = Math.toRadians(degrees);
                BlockPos pos = SpawnLocationHelper.computeTargetPosition(0, 0, angle, 500,
                        CONFIGURED_MAX_WORLD_RADIUS);
                assertTrue(
                        pos.getX() >= -CONFIGURED_MAX_WORLD_RADIUS + 1
                                && pos.getX() <= CONFIGURED_MAX_WORLD_RADIUS - 1,
                        "X out of bounds at " + degrees + " degrees: " + pos.getX());
                assertTrue(
                        pos.getZ() >= -CONFIGURED_MAX_WORLD_RADIUS + 1
                                && pos.getZ() <= CONFIGURED_MAX_WORLD_RADIUS - 1,
                        "Z out of bounds at " + degrees + " degrees: " + pos.getZ());
            }
        }

        @Test
        void playerAlreadyAtBorder() {
            BlockPos pos = SpawnLocationHelper.computeTargetPosition(287, 287, 0, 50, CONFIGURED_MAX_WORLD_RADIUS);
            assertEquals(CONFIGURED_MAX_WORLD_RADIUS - 1, pos.getX());
            assertTrue(pos.getZ() <= CONFIGURED_MAX_WORLD_RADIUS - 1);
        }
    }

    // --- computePositionInFrontOf ---

    @Nested
    class PositionInFrontOf {

        @Test
        void offsetAlongLookDirectionEast() {
            // Looking east: lookAngleX=1, lookAngleZ=0
            Vec3 pos = SpawnLocationHelper.computePositionInFrontOf(10, 65, 20, 1.0, 0.0, 3);
            assertEquals(13.0, pos.x, 0.001, "X should be player + distance * lookAngleX");
            assertEquals(68.0, pos.y, 0.001, "Y should be player + distance");
            assertEquals(20.0, pos.z, 0.001, "Z should be unchanged when lookAngleZ is 0");
        }

        @Test
        void offsetAlongLookDirectionSouth() {
            // Looking south: lookAngleX=0, lookAngleZ=1
            Vec3 pos = SpawnLocationHelper.computePositionInFrontOf(10, 65, 20, 0.0, 1.0, 3);
            assertEquals(10.0, pos.x, 0.001, "X should be unchanged when lookAngleX is 0");
            assertEquals(68.0, pos.y, 0.001, "Y should be player + distance");
            assertEquals(23.0, pos.z, 0.001, "Z should be player + distance * lookAngleZ");
        }

        @Test
        void diagonalLookDirection() {
            // Looking northeast at 45 degrees: lookAngleX ≈ 0.707, lookAngleZ ≈ -0.707
            double d = Math.sqrt(2) / 2;
            Vec3 pos = SpawnLocationHelper.computePositionInFrontOf(0, 70, 0, d, -d, 4);
            assertEquals(4 * d, pos.x, 0.001, "X should be offset by distance * lookAngleX");
            assertEquals(74.0, pos.y, 0.001, "Y should be player + distance");
            assertEquals(-4 * d, pos.z, 0.001, "Z should be offset by distance * lookAngleZ");
        }

        @Test
        void yAlwaysRisesByDistance() {
            // Y is always player.Y + distance, regardless of look angle
            Vec3 pos3 = SpawnLocationHelper.computePositionInFrontOf(0, 100, 0, 0, 0, 3);
            Vec3 pos6 = SpawnLocationHelper.computePositionInFrontOf(0, 100, 0, 0, 0, 6);
            assertEquals(103.0, pos3.y, 0.001, "Y should rise by 3");
            assertEquals(106.0, pos6.y, 0.001, "Y should rise by 6");
        }

        @Test
        void distanceSixForExplosionSpawn() {
            // The explode method uses distance=6
            Vec3 pos = SpawnLocationHelper.computePositionInFrontOf(50, 80, 50, 1.0, 0.0, 6);
            assertEquals(56.0, pos.x, 0.001);
            assertEquals(86.0, pos.y, 0.001);
            assertEquals(50.0, pos.z, 0.001);
        }

        @Test
        void distanceThreeForChickenSpawn() {
            // The event method uses distance=3
            Vec3 pos = SpawnLocationHelper.computePositionInFrontOf(50, 80, 50, 1.0, 0.0, 3);
            assertEquals(53.0, pos.x, 0.001);
            assertEquals(83.0, pos.y, 0.001);
            assertEquals(50.0, pos.z, 0.001);
        }

        @Test
        void negativePlayerPosition() {
            Vec3 pos = SpawnLocationHelper.computePositionInFrontOf(-100, 40, -200, 1.0, 1.0, 5);
            assertEquals(-95.0, pos.x, 0.001);
            assertEquals(45.0, pos.y, 0.001);
            assertEquals(-195.0, pos.z, 0.001);
        }
    }
}
