package org.me.pyke.pyautopickup.WorldBounding;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.jetbrains.annotations.Nullable;
import org.me.pyke.pyautopickup.PyAutoPickup;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

public class WorldBoundingBoxGenerator {

    private static final Predicate<Entity> HANGING_PREDICATE = entity -> entity instanceof Hanging;
    private static final HashSet<Material> BLOCKS_NEEDING_A_LONG_TIME = new HashSet<>();

    static {
        for (String matName : new String[]{"CHORUS_PLANT", "KELP_PLANT"}) {
            Material mat = Material.getMaterial(matName);
            if (mat != null) {
                BLOCKS_NEEDING_A_LONG_TIME.add(mat);
            }
        }
    }

    private final Block block;
    private final Location location;

    public WorldBoundingBoxGenerator(Location location, Block block) {
        this.location = location;
        this.block = block;
    }

    public static WorldBoundingBox getAppropriateBoundingBox(Location location, @Nullable Block block) {
        return new WorldBoundingBoxGenerator(location, block).get();
    }

    private WorldBoundingBox get() {
        PyAutoPickup plugin = PyAutoPickup.getInstance();
        int lifeTime = 2;
        int radius = plugin.getConfig().getInt("default-bounding-box-radius", 1);
        int radiusYMin = plugin.getConfig().getInt("default-bounding-box-radius-ymin", 1);
        int radiusYMax = plugin.getConfig().getInt("default-bounding-box-radius-ymax", 1);

        if (block != null) {
            Material mat = block.getType();

            if (block.getRelative(BlockFace.UP).getType().hasGravity()) {
                BoundingBoxPrediction prediction = getGravityBlocksAbove(block.getRelative(BlockFace.UP));
                radiusYMax = Math.max(radiusYMax, prediction.getHeightNeeded());
                lifeTime += prediction.getLifetimeNeeded();
            }

            if (BLOCKS_NEEDING_A_LONG_TIME.contains(mat)) {
                lifeTime += 500;
                radiusYMax += 20;
                radius += 10;
            }

            if (hasHangingsAttached(block)) {
                lifeTime += 200;
            }
        }

        return new WorldBoundingBox(location, lifeTime, radius, radiusYMin, radiusYMax);
    }

    private boolean hasHangingsAttached(Block block) {
        Collection<Entity> nearbyHangings = block.getWorld().getNearbyEntities(
                block.getLocation().add(0.5, 0.5, 0.5),
                1,
                1,
                1,
                HANGING_PREDICATE
        );
        for (Entity entity : nearbyHangings) {
            BlockFace attachedFace = ((Hanging) entity).getAttachedFace();
            if (entity.getLocation().getBlock().getRelative(attachedFace).equals(block)) {
                return true;
            }
        }
        return false;
    }

    private BoundingBoxPrediction getGravityBlocksAbove(Block block) {
        Block current = block;
        int extraLifetimeNeeded = 0;
        int height = 0;
        while (extraLifetimeNeeded < 256 && current.getType().hasGravity()) {
            extraLifetimeNeeded += 2;
            height += 1;
            current = current.getRelative(BlockFace.UP);
        }
        return new BoundingBoxPrediction(Math.max(0, height), Math.max(0, extraLifetimeNeeded));
    }
}
