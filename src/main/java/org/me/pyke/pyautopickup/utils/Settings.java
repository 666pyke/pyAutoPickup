package org.me.pyke.pyautopickup.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.Set;

public class Settings {

    public static final String USE_PERMISSION = "pyautopickup.use";
    public static final String RELOAD_PERMISSION = "pyautopickup.reload";
    public static final String UPDATE_PERMISSION = "pyautopickup.update";

    private boolean pluginEnabled;
    private boolean blockPickupEnabled;
    private boolean mobDropPickupEnabled;
    private boolean creativePickupEnabled;
    private boolean permissionRequired;
    private boolean defaultPickupEnabled;
    private boolean bStatsEnabled;
    private boolean debugEnabled;
    private boolean itemSpawnIgnoreCancelled;
    private boolean fullInventoryChatEnabled;
    private boolean fullInventoryTitleEnabled;
    private boolean updateCheckerEnabled;
    private int defaultBoxRadius;
    private int defaultBoxYMin;
    private int defaultBoxYMax;
    private EventPriority itemSpawnPriority;
    private Set<String> blacklistedWorlds = new HashSet<>();

    public void reload(FileConfiguration config) {
        pluginEnabled = config.getBoolean("plugin-enabled", true);
        bStatsEnabled = config.getBoolean("bstats-enabled", true);

        blockPickupEnabled = config.getBoolean("autopickup.blocks", true);
        mobDropPickupEnabled = config.getBoolean("autopickup.mob-drops", true);
        creativePickupEnabled = config.getBoolean("autopickup.works-in-creative", true);
        permissionRequired = config.getBoolean("autopickup.require-permission", false);
        defaultPickupEnabled = config.getBoolean("autopickup.default-enabled", false);

        defaultBoxRadius = config.getInt("default-bounding-box-radius", 1);
        defaultBoxYMin = config.getInt("default-bounding-box-radius-ymin", 1);
        defaultBoxYMax = config.getInt("default-bounding-box-radius-ymax", 1);

        itemSpawnPriority = parsePriority(config.getString("advanced.item-spawn-event-priority", "NORMAL"));
        itemSpawnIgnoreCancelled = config.getBoolean("advanced.item-spawn-ignore-cancelled", true);
        debugEnabled = config.getBoolean("advanced.debug", false);

        fullInventoryChatEnabled = config.getBoolean("full-inventory.chat-message", true);
        fullInventoryTitleEnabled = config.getBoolean("full-inventory.title-message", true);

        updateCheckerEnabled = config.getBoolean("update-checker", true);

        blacklistedWorlds = new HashSet<>(config.getStringList("blacklisted-worlds"));
    }

    private EventPriority parsePriority(String rawPriority) {
        if (rawPriority == null || rawPriority.trim().isEmpty()) {
            return EventPriority.NORMAL;
        }
        try {
            return EventPriority.valueOf(rawPriority.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return EventPriority.NORMAL;
        }
    }

    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    public boolean isBlockPickupEnabled() {
        return blockPickupEnabled;
    }

    public boolean isMobDropPickupEnabled() {
        return mobDropPickupEnabled;
    }

    public boolean isCreativePickupEnabled() {
        return creativePickupEnabled;
    }

    public boolean isPermissionRequired() {
        return permissionRequired;
    }

    public boolean isDefaultPickupEnabled() {
        return defaultPickupEnabled;
    }

    public boolean isBStatsEnabled() {
        return bStatsEnabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean isItemSpawnIgnoreCancelled() {
        return itemSpawnIgnoreCancelled;
    }

    public boolean isFullInventoryChatEnabled() {
        return fullInventoryChatEnabled;
    }

    public boolean isFullInventoryTitleEnabled() {
        return fullInventoryTitleEnabled;
    }

    public boolean isUpdateCheckerEnabled() {
        return updateCheckerEnabled;
    }

    public int getDefaultBoxRadius() {
        return defaultBoxRadius;
    }

    public int getDefaultBoxYMin() {
        return defaultBoxYMin;
    }

    public int getDefaultBoxYMax() {
        return defaultBoxYMax;
    }

    public EventPriority getItemSpawnPriority() {
        return itemSpawnPriority;
    }

    public boolean isWorldBlacklisted(String worldName) {
        return blacklistedWorlds.contains(worldName);
    }
}
