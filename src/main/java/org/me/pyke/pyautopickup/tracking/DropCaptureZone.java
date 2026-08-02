package org.me.pyke.pyautopickup.tracking;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.util.Comparator;
import java.util.Objects;

public class DropCaptureZone {

    private final BoundingBox bounds;
    private final World world;
    private int ticksRemaining;

    public DropCaptureZone(Location origin, int ticksRemaining, int horizontalRadius, int yBelow, int yAbove) {
        this.ticksRemaining = ticksRemaining;
        this.world = origin.getWorld();
        this.bounds = BoundingBox.of(
                origin.getBlock().getRelative(-horizontalRadius, -yBelow, -horizontalRadius),
                origin.getBlock().getRelative(horizontalRadius, yAbove, horizontalRadius)
        );
    }

    public DropCaptureZone(World world, int ticksRemaining, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.ticksRemaining = ticksRemaining;
        this.world = world;
        this.bounds = BoundingBox.of(
                world.getBlockAt(minX, minY, minZ),
                world.getBlockAt(maxX, maxY, maxZ)
        );
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public World getWorld() {
        return world;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public boolean contains(Location location) {
        return location.getWorld().equals(world) && bounds.contains(location.toVector());
    }

    public boolean tickAndExpire() {
        ticksRemaining--;
        return ticksRemaining <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DropCaptureZone that = (DropCaptureZone) o;
        return Objects.equals(bounds, that.bounds) && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bounds, world);
    }

    @Override
    public String toString() {
        return "DropCaptureZone{" +
                "bounds=" + bounds +
                ", world=" + world +
                ", ticksRemaining=" + ticksRemaining +
                '}';
    }

    public static class DistanceComparator implements Comparator<DropCaptureZone> {

        private final Location location;

        public DistanceComparator(Location location) {
            this.location = location;
        }

        @Override
        public int compare(DropCaptureZone first, DropCaptureZone second) {
            return Double.compare(
                    first.bounds.getCenter().distanceSquared(location.toVector()),
                    second.bounds.getCenter().distanceSquared(location.toVector())
            );
        }
    }
}
