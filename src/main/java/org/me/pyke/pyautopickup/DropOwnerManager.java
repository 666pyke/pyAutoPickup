package org.me.pyke.pyautopickup;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.me.pyke.pyautopickup.tracking.DropCaptureZone;
import org.me.pyke.pyautopickup.tracking.DropZoneFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class DropOwnerManager {

    private final HashMap<DropCaptureZone, UUID> dropLocationMap = new HashMap<>();
    private final PyAutoPickup plugin;

    public DropOwnerManager(PyAutoPickup plugin) {
        this.plugin = plugin;
    }

    public void initializeScheduler() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Iterator<DropCaptureZone> iterator = dropLocationMap.keySet().iterator();
            while (iterator.hasNext()) {
                DropCaptureZone zone = iterator.next();
                if (zone.tickAndExpire()) {
                    iterator.remove();
                }
            }
        }, 20L, 20L);
    }

    public Player getDropOwner(Location location) {
        Player player = getDropOwner(location, dropLocationMap);
        if (plugin.getSettings().isDebugEnabled() && player == null) {
            plugin.getLogger().info("[Debug] No owner matched for item at "
                    + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
                    + " in " + location.getWorld().getName()
                    + " activeBoxes=" + dropLocationMap.size());
        }
        if (player == null || player.isDead()) return null;
        return player;
    }

    public void register(Player player, Location location) {
        DropCaptureZone zone = DropZoneFactory.create(location, null);
        dropLocationMap.put(zone, player.getUniqueId());
        debugRegistered(player, location, zone);
    }

    public void register(Player player, Location location, Block block) {
        DropCaptureZone zone = DropZoneFactory.create(location, block);
        dropLocationMap.put(zone, player.getUniqueId());
        debugRegistered(player, location, zone);
    }

    private Player getDropOwner(Location location, HashMap<DropCaptureZone, UUID> map) {
        DropCaptureZone match = null;
        double bestDistance = Double.MAX_VALUE;
        for (DropCaptureZone zone : map.keySet()) {
            if (zone.contains(location)) {
                double distance = zone.getBounds().getCenter().distanceSquared(location.toVector());
                if (distance < bestDistance) {
                    match = zone;
                    bestDistance = distance;
                }
            }
        }
        return match == null ? null : Bukkit.getPlayer(map.get(match));
    }

    private void debugRegistered(Player player, Location location, DropCaptureZone zone) {
        if (plugin.getSettings().isDebugEnabled()) {
            plugin.getLogger().info("[Debug] Registered box for " + player.getName()
                    + " at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
                    + " ticksLeft=" + zone.getTicksRemaining()
                    + " activeBoxes=" + dropLocationMap.size());
        }
    }
}
