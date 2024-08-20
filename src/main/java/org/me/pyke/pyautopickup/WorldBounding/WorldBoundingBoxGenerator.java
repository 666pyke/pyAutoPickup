package org.me.pyke.pyautopickup.WorldBounding;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.me.pyke.pyautopickup.PyAutoPickup;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class WorldBoundingBoxGenerator {

    private static final Set<Material> unstable = new HashSet<>();
    private final Block block;
    private final Location location;
    private final Player player;
    private static final PyAutoPickup plugin = PyAutoPickup.getInstance();
    private static final Predicate<Entity> HANGING_PREDICATE = entity -> entity instanceof Hanging;
    private static final HashSet<Material> BLOCKS_NEEDING_A_LONG_TIME = new HashSet<>();

    static {
        // Inițializăm lista cu blocuri care necesită mai mult timp pentru a cădea (de ex. Chorus Plant)
        for (String matName : new String[]{"CHORUS_PLANT", "KELP_PLANT"}) {
            Material mat = Material.getMaterial(matName);
            if (mat != null) {
                BLOCKS_NEEDING_A_LONG_TIME.add(mat);
            }
        }
    }

    public static WorldBoundingBox getSimpleBoundingBox(Location location) {
        return new WorldBoundingBox(location, 2, 1, 1, 1);
    }

    public WorldBoundingBoxGenerator(Location location, Block block, Player player) {
        this.location = location;
        this.block = block;
        this.player = player;
    }

    public static WorldBoundingBox getAppropriateBoundingBox(Location location, @Nullable Block block, Player player) {
        return new WorldBoundingBoxGenerator(location, block, player).get();
    }

    private WorldBoundingBox get() {
        int lifeTime = 2; // Durata de viață implicită
        int radius = plugin.getConfig().getInt("default-bounding-box-radius", 1);
        int radiusYMin = plugin.getConfig().getInt("default-bounding-box-radius-ymin", 0);
        int radiusYMax = plugin.getConfig().getInt("default-bounding-box-radius-ymax", 1);

        if (block != null) {
            Material mat = block.getType();

            if (isUnstable(mat) || isUnstable(block.getRelative(BlockFace.UP).getType())) {
                BoundingBoxPrediction boundingBoxPrediction = getUnstableBlocksAbove(block.getRelative(BlockFace.UP));
                radiusYMax = boundingBoxPrediction.getHeightNeeded();
                lifeTime += boundingBoxPrediction.getLifetimeNeeded();
            }

            // Ajustăm timpul de viață și mărimea bounding box-ului pentru blocuri speciale
            if (BLOCKS_NEEDING_A_LONG_TIME.contains(mat)) {
                lifeTime += 500;
                radiusYMax += 20;
                radius += 10;
            }
        }

        // Verificăm entitățile de tip Hanging atașate blocului
        if (block != null && hasHangingsAttached(block)) {
            lifeTime += 200;
        }

        return new WorldBoundingBox(location, lifeTime, radius, radiusYMin, radiusYMax);
    }

    private boolean hasHangingsAttached(Block block) {
        Collection<Entity> nearbyHangings = block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), 1, 1, 1, HANGING_PREDICATE);
        for (Entity entity : nearbyHangings) {
            BlockFace attachedFace = ((Hanging) entity).getAttachedFace();
            if (entity.getLocation().getBlock().getRelative(attachedFace).equals(block)) {
                return true;
            }
        }
        return false;
    }

    private BoundingBoxPrediction getUnstableBlocksAbove(Block block) {
        Block current = block;
        int extraLifetimeNeeded = 0;
        int height = 0;
        int gravityBlocksAbove = 0;
        while (extraLifetimeNeeded < 256) {
            if (isUnstable(current.getType())) {
                extraLifetimeNeeded += 1;
                height += 1;
                gravityBlocksAbove = 0;
            } else if (current.getType().hasGravity()) {
                extraLifetimeNeeded += 2;
                height += 1;
                gravityBlocksAbove += 1;
            } else {
                extraLifetimeNeeded -= gravityBlocksAbove * 2;
                height -= gravityBlocksAbove;
                break;
            }
            current = current.getRelative(BlockFace.UP);
        }
        return new BoundingBoxPrediction(height, extraLifetimeNeeded);
    }

    private boolean isUnstable(Material mat) {
        return mat.isAir() || unstable.contains(mat);
    }
}
