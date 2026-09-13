package org.me.pyke.pyautopickup.tracking;

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
import java.util.Set;
import java.util.function.Predicate;

public class DropZoneFactory {

    private static final Predicate<Entity> HANGING_ENTITY = entity -> entity instanceof Hanging;
    private static final Set<Material> SLOW_DROP_BLOCKS = new HashSet<>();

    static {
        addIfPresent("CHORUS_PLANT");
        addIfPresent("KELP_PLANT");
    }

    private final Location origin;
    private final Block sourceBlock;

    private DropZoneFactory(Location origin, @Nullable Block sourceBlock) {
        this.origin = origin;
        this.sourceBlock = sourceBlock;
    }

    public static DropCaptureZone create(Location origin, @Nullable Block sourceBlock) {
        return new DropZoneFactory(origin, sourceBlock).build();
    }

    private static void addIfPresent(String materialName) {
        Material material = Material.getMaterial(materialName);
        if (material != null) {
            SLOW_DROP_BLOCKS.add(material);
        }
    }

    private DropCaptureZone build() {
        PyAutoPickup plugin = PyAutoPickup.getInstance();

        int lifetimeTicks = 2;
        int horizontalRadius = plugin.getSettings().getDefaultBoxRadius();
        int yBelow = plugin.getSettings().getDefaultBoxYMin();
        int yAbove = plugin.getSettings().getDefaultBoxYMax();

        if (sourceBlock != null) {
            Material sourceType = sourceBlock.getType();

            ZoneExpansion gravityExpansion = scanFallingColumn(sourceBlock.getRelative(BlockFace.UP));
            yAbove = Math.max(yAbove, gravityExpansion.getVerticalReach());
            lifetimeTicks += gravityExpansion.getExtraTicks();

            if (SLOW_DROP_BLOCKS.contains(sourceType)) {
                lifetimeTicks += 500;
                yAbove += 20;
                horizontalRadius += 10;
            }

            if (hasAttachedHanging(sourceBlock)) {
                lifetimeTicks += 200;
            }
        }

        return new DropCaptureZone(origin, lifetimeTicks, horizontalRadius, yBelow, yAbove);
    }

    private boolean hasAttachedHanging(Block block) {
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        Collection<Entity> entities = block.getWorld().getNearbyEntities(center, 1, 1, 1, HANGING_ENTITY);

        for (Entity entity : entities) {
            BlockFace attachedFace = ((Hanging) entity).getAttachedFace();
            if (entity.getLocation().getBlock().getRelative(attachedFace).equals(block)) {
                return true;
            }
        }

        return false;
    }

    private ZoneExpansion scanFallingColumn(Block firstBlock) {
        Block current = firstBlock;
        int extraTicks = 0;
        int height = 0;

        while (extraTicks < 256 && current.getType().hasGravity()) {
            extraTicks += 2;
            height++;
            current = current.getRelative(BlockFace.UP);
        }

        return new ZoneExpansion(height, extraTicks);
    }
}
