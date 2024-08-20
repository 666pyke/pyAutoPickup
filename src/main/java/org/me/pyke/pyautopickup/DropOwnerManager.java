package org.me.pyke.pyautopickup;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.me.pyke.pyautopickup.WorldBounding.WorldBoundingBox;
import org.me.pyke.pyautopickup.WorldBounding.WorldBoundingBoxGenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class DropOwnerManager {

    private final static HashMap<WorldBoundingBox, UUID> dropLocationMap = new HashMap<>();
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
                    //plugin.getLogger().info("BoundingBox expired and removed");
                }
            }
        }, 20L, 20L); // Verificăm o dată pe secundă (20 ticks)
    }

    public Player getDropOwner(Location location) {
        Player player = getDropOwner(location, dropLocationMap);
        if (player == null || player.isDead()) return null;
        return player;
    }

    public void register(Player player, Location location) {
        WorldBoundingBox boundingBox = WorldBoundingBoxGenerator.getSimpleBoundingBox(location);
        dropLocationMap.put(boundingBox, player.getUniqueId());
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
}
