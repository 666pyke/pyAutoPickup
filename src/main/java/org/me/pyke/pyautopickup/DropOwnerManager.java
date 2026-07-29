package org.me.pyke.pyautopickup;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.me.pyke.pyautopickup.WorldBounding.WorldBoundingBox;
import org.me.pyke.pyautopickup.WorldBounding.WorldBoundingBoxGenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class DropOwnerManager {

    private final HashMap<WorldBoundingBox, UUID> dropLocationMap = new HashMap<>();
    private final PyAutoPickup plugin;

    public DropOwnerManager(PyAutoPickup plugin) {
        this.plugin = plugin;
    }

    public void initializeScheduler() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Iterator<WorldBoundingBox> iterator = dropLocationMap.keySet().iterator();
            while (iterator.hasNext()) {
                WorldBoundingBox boundingBox = iterator.next();
                if (boundingBox.isExpired()) {
                    iterator.remove();
                }
            }
        }, 20L, 20L);
    }

    public Player getDropOwner(Location location) {
        Player player = getDropOwner(location, dropLocationMap);
        if (plugin.getConfig().getBoolean("advanced.debug", false) && player == null) {
            plugin.getLogger().info("[Debug] No owner matched for item at "
                    + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
                    + " in " + location.getWorld().getName()
                    + " activeBoxes=" + dropLocationMap.size());
        }
        if (player == null || player.isDead()) return null;
        return player;
    }

    public void register(Player player, Location location) {
        WorldBoundingBox boundingBox = WorldBoundingBoxGenerator.getAppropriateBoundingBox(location, null);
        dropLocationMap.put(boundingBox, player.getUniqueId());
        debugRegistered(player, location, boundingBox);
    }

    public void register(Player player, Location location, Block block) {
        WorldBoundingBox boundingBox = WorldBoundingBoxGenerator.getAppropriateBoundingBox(location, block);
        dropLocationMap.put(boundingBox, player.getUniqueId());
        debugRegistered(player, location, boundingBox);
    }

    private Player getDropOwner(Location location, HashMap<WorldBoundingBox, UUID> map) {
        WorldBoundingBox match = null;
        double bestDistance = Double.MAX_VALUE;
        for (WorldBoundingBox boundingBox : map.keySet()) {
            if (boundingBox.contains(location)) {
                double distance = boundingBox.getBoundingBox().getCenter().distanceSquared(location.toVector());
                if (distance < bestDistance) {
                    match = boundingBox;
                    bestDistance = distance;
                }
            }
        }
        return match == null ? null : Bukkit.getPlayer(map.get(match));
    }

    private void debugRegistered(Player player, Location location, WorldBoundingBox boundingBox) {
        if (plugin.getConfig().getBoolean("advanced.debug", false)) {
            plugin.getLogger().info("[Debug] Registered box for " + player.getName()
                    + " at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
                    + " ticksLeft=" + boundingBox.getTicksLeft()
                    + " activeBoxes=" + dropLocationMap.size());
        }
    }
}
