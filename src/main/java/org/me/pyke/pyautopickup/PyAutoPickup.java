package org.me.pyke.pyautopickup;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.me.pyke.pyautopickup.listener.EventListener;
import org.me.pyke.pyautopickup.listener.SessionListener;
import org.me.pyke.pyautopickup.storage.Database;
import org.me.pyke.pyautopickup.storage.ToggleService;
import org.me.pyke.pyautopickup.utils.Lang;

import java.io.File;
import java.util.Objects;

public final class PyAutoPickup extends JavaPlugin {

    private static PyAutoPickup instance;
    private DropOwnerManager dropOwnerManager;

    private Database database;
    private ToggleService toggleService;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Failed to create plugin data folder: " + getDataFolder().getAbsolutePath());
        }

        saveDefaultConfig();
        Lang.init(getConfig());

        dropOwnerManager = new DropOwnerManager(this);
        dropOwnerManager.initializeScheduler();

        database = new Database(new File(getDataFolder(), "pyautopickup.db").getAbsolutePath());
        try {
            database.open();
        } catch (Exception e) {
            getLogger().severe("Failed to open SQLite database: " + e.getMessage());
        }

        toggleService = new ToggleService(this, database);

        for (Player p : Bukkit.getOnlinePlayers()) {
            toggleService.warmup(p.getUniqueId());
        }

        EventPriority itemSpawnPriority = getItemSpawnEventPriority();
        new EventListener(this, itemSpawnPriority).register();
        getLogger().info("ItemSpawnEvent priority: " + itemSpawnPriority.name());
        getServer().getPluginManager().registerEvents(new SessionListener(this), this);

        if (getConfig().getBoolean("bstats-enabled", true)) {
            new Metrics(this, 32965);
        }

        CommandManager commandManager = new CommandManager(this);
        Objects.requireNonNull(this.getCommand("pyautopickup")).setExecutor(commandManager);
        Objects.requireNonNull(this.getCommand("pyautopickup")).setTabCompleter(commandManager);
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
    }

    public static PyAutoPickup getInstance() {
        return instance;
    }

    public DropOwnerManager getDropOwnerManager() {
        return dropOwnerManager;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        Lang.init(getConfig());
        getLogger().info("Config reloaded!");
    }

    public ToggleService getToggleService() {
        return toggleService;
    }

    public EventPriority getItemSpawnEventPriority() {
        String rawPriority = getConfig().getString("advanced.item-spawn-event-priority", "NORMAL");
        try {
            return EventPriority.valueOf(rawPriority.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid advanced.item-spawn-event-priority '" + rawPriority + "'. Using NORMAL.");
            return EventPriority.NORMAL;
        }
    }
}
