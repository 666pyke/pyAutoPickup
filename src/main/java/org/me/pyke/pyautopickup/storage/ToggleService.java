package org.me.pyke.pyautopickup.storage;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ToggleService {

    public static class Prefs {
        public volatile boolean enabled = true;
        public volatile boolean notifyEnabled = true;
    }

    private final Plugin plugin;
    private final Database db;
    private final Map<UUID, Prefs> cache = new ConcurrentHashMap<>();

    public ToggleService(Plugin plugin, Database db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void warmup(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Prefs p = cache.computeIfAbsent(uuid, k -> new Prefs());
                p.enabled = db.getEnabled(uuid);
                p.notifyEnabled = db.getNotifyEnabled(uuid);
            } catch (SQLException e) {
                plugin.getLogger().warning("Could not load autopickup preferences for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public boolean isPickupEnabled(UUID uuid) {
        return cache.getOrDefault(uuid, new Prefs()).enabled;
    }

    public boolean isNotifyEnabled(UUID uuid) {
        return cache.getOrDefault(uuid, new Prefs()).notifyEnabled;
    }

    public void setPickupEnabled(UUID uuid, boolean v) {
        cache.computeIfAbsent(uuid, k -> new Prefs()).enabled = v;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                db.setEnabled(uuid, v);
            } catch (SQLException e) {
                plugin.getLogger().warning("Could not save autopickup toggle for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public boolean togglePickup(UUID uuid) {
        boolean next = !isPickupEnabled(uuid);
        setPickupEnabled(uuid, next);
        return next;
    }

    public void setNotifyEnabled(UUID uuid, boolean v) {
        cache.computeIfAbsent(uuid, k -> new Prefs()).notifyEnabled = v;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                db.setNotifyEnabled(uuid, v);
            } catch (SQLException e) {
                plugin.getLogger().warning("Could not save autopickup message toggle for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public boolean toggleNotify(UUID uuid) {
        boolean next = !isNotifyEnabled(uuid);
        setNotifyEnabled(uuid, next);
        return next;
    }
}
