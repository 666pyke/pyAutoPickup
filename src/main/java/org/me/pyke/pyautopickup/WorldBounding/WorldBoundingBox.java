package org.me.pyke.pyautopickup.WorldBounding;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

import java.util.Comparator;
import java.util.Objects;

/**
 * Reprezintă zona în care dropurile cauzate de un jucător pot apărea. Când lifeTime ajunge la zero, această zonă este eliminată din hartă.
 */
public class WorldBoundingBox {

    private final BoundingBox boundingBox;
    private final World world;
    private int lifeTime;

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public World getWorld() {
        return world;
    }

    public WorldBoundingBox(Location location, int lifeTime, int radius, int radiusYMin, int radiusYMax) {
        this.lifeTime = lifeTime;
        this.boundingBox = BoundingBox.of(
                location.getBlock().getRelative(-radius, -radiusYMin, -radius),
                location.getBlock().getRelative(radius, radiusYMax, radius)
        );
        this.world = location.getWorld();
    }

    public WorldBoundingBox(World world, int lifeTime, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.lifeTime = lifeTime;
        this.world = world;
        this.boundingBox = BoundingBox.of(
                world.getBlockAt(minX, minY, minZ),
                world.getBlockAt(maxX, maxY, maxZ)
        );
    }

    public boolean contains(Location location) {
        if (!location.getWorld().equals(world)) return false;
        return boundingBox.contains(location.toVector());
    }

    public int getTicksLeft() {
        return lifeTime;
    }

    public boolean isExpired() {
        lifeTime--;
        return lifeTime <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldBoundingBox that = (WorldBoundingBox) o;
        return Objects.equals(boundingBox, that.boundingBox) && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boundingBox, world);
    }

    @Override
    public String toString() {
        return "WorldBoundingBox{" +
                "boundingBox=" + boundingBox +
                ", world=" + world +
                ", lifeTime=" + lifeTime +
                '}';
    }

    public static class BoundingBoxComparator implements Comparator<WorldBoundingBox> {

        private final Location location;

        public BoundingBoxComparator(Location location) {
            this.location = location;
        }

        @Override
        public int compare(WorldBoundingBox o1, WorldBoundingBox o2) {
            return Double.compare(
                    o1.boundingBox.getCenter().distanceSquared(location.toVector()),
                    o2.boundingBox.getCenter().distanceSquared(location.toVector())
            );
        }
    }
}
