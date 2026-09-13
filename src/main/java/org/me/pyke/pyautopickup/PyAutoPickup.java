package org.me.pyke.pyautopickup;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.me.pyke.pyautopickup.listener.EventListener;
import org.me.pyke.pyautopickup.listener.SessionListener;
import org.me.pyke.pyautopickup.storage.Database;
import org.me.pyke.pyautopickup.storage.ToggleService;
import org.me.pyke.pyautopickup.utils.Lang;
import org.me.pyke.pyautopickup.utils.Settings;
import org.me.pyke.pyautopickup.utils.UpdateChecker;

import java.io.File;
import java.util.Objects;

public final class PyAutoPickup extends JavaPlugin {

    private static PyAutoPickup instance;
    private DropOwnerManager dropOwnerManager;
    private Settings settings;
    private EventListener eventListener;
    private UpdateChecker updateChecker;

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
        settings = new Settings();
        settings.reload(getConfig());

        dropOwnerManager = new DropOwnerManager(this);
        dropOwnerManager.initializeScheduler();

        database = new Database(new File(getDataFolder(), "pyautopickup.db").getAbsolutePath());
        try {
            database.open();
        } catch (Exception e) {
            getLogger().severe("Failed to open SQLite database: " + e.getMessage());
        }

        toggleService = new ToggleService(this, database, settings.isDefaultPickupEnabled());

        for (Player p : Bukkit.getOnlinePlayers()) {
            toggleService.warmup(p.getUniqueId());
        }

        eventListener = new EventListener(this);
        eventListener.register();
        getLogger().info("ItemSpawnEvent priority: " + settings.getItemSpawnPriority().name());
        getServer().getPluginManager().registerEvents(new SessionListener(this), this);

        if (settings.isBStatsEnabled()) {
            new Metrics(this, 32965);
        }

        updateChecker = new UpdateChecker(this);
        Bukkit.getScheduler().runTaskLater(this, () -> updateChecker.checkAsync(), 60L);

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
        settings.reload(getConfig());
        toggleService.setDefaultPickupEnabled(settings.isDefaultPickupEnabled());
        for (Player p : Bukkit.getOnlinePlayers()) {
            toggleService.warmup(p.getUniqueId());
        }
        eventListener.register();
        updateChecker.checkAsync();
        getLogger().info("Config reloaded!");
    }

    public ToggleService getToggleService() {
        return toggleService;
    }

    public Settings getSettings() {
        return settings;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
