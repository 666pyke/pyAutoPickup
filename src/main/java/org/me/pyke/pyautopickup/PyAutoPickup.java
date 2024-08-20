package org.me.pyke.pyautopickup;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PyAutoPickup extends JavaPlugin {

    private static PyAutoPickup instance;
    private DropOwnerManager dropOwnerManager;

    @Override
    public void onEnable() {
        instance = this;

        dropOwnerManager = new DropOwnerManager(this);
        dropOwnerManager.initializeScheduler();

        // Registering listeners
        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        saveDefaultConfig();

        // Registering commands
        CommandManager commandManager = new CommandManager(this);
        Objects.requireNonNull(this.getCommand("pyautopickup")).setExecutor(commandManager);
        Objects.requireNonNull(this.getCommand("pyautopickup")).setTabCompleter(commandManager);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static PyAutoPickup getInstance() {
        return instance;
    }

    public DropOwnerManager getDropOwnerManager() {
        return dropOwnerManager;
    }

    // Reload config
    public void reloadPluginConfig() {
        reloadConfig();
        getLogger().info("Config reloaded!");
    }
}
