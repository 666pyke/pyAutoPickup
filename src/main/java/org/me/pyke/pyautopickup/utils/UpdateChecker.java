package org.me.pyke.pyautopickup.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.me.pyke.pyautopickup.PyAutoPickup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UpdateChecker {

    private static final int SPIGOT_RESOURCE_ID = 119030;
    private static final int BUILT_BY_BIT_RESOURCE_ID = 119126;
    private static final String SPIGOT_RESOURCE_URL = "https://www.spigotmc.org/resources/pyautopickup." + SPIGOT_RESOURCE_ID + "/";
    private static final String BUILT_BY_BIT_RESOURCE_URL = "https://builtbybit.com/resources/pyautopickup." + BUILT_BY_BIT_RESOURCE_ID + "/";
    private static final String DISCORD_LINK = "https://discord.gg/bvEQ6DRuvv";
    private static final String SPIGOT_UPDATE_URL = "https://api.spigotmc.org/legacy/update.php?resource=" + SPIGOT_RESOURCE_ID;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final PyAutoPickup plugin;
    private volatile CheckResult latestResult = CheckResult.none();

    public UpdateChecker(PyAutoPickup plugin) {
        this.plugin = plugin;
    }

    public void checkAsync() {
        if (!plugin.getSettings().isUpdateCheckerEnabled()) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CheckResult result = checkNow();
            latestResult = result;

            if (result.isUpdateAvailable()) {
                plugin.getLogger().info("New update available: " + result.getLatestVersion()
                        + " (current: " + getCurrentVersion() + ")");
            }
        });
    }

    public void notifyPlayerLater(Player player) {
        if (!plugin.getSettings().isUpdateCheckerEnabled()) return;
        if (!player.hasPermission(Settings.UPDATE_PERMISSION)) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                notifyIfOutdated(player);
            }
        }, 60L);
    }

    public void notifyIfOutdated(CommandSender sender) {
        CheckResult result = latestResult;
        if (!result.isUpdateAvailable()) return;

        sender.sendMessage("");
        sender.sendMessage(Lang.prefixedRaw("&#ff8fd2New update available!"));
        sender.sendMessage(Lang.prefixedRaw("&fCurrent version: &#ff5ebc" + getCurrentVersion()
                + " &8| &fLatest: &#ff8fd2" + result.getLatestVersion()));
        sender.sendMessage(Lang.prefixedRaw("&fSpigotMC: &#ff8fd2" + SPIGOT_RESOURCE_URL));
        sender.sendMessage(Lang.prefixedRaw("&fBuiltByBit: &#ff8fd2" + BUILT_BY_BIT_RESOURCE_URL));
        sender.sendMessage("");
        sender.sendMessage(Lang.color("&7&oRunning into problems? Join " + DISCORD_LINK));
        sender.sendMessage("");
    }

    private CheckResult checkNow() {
        return checkSpigot();
    }

    private CheckResult checkSpigot() {
        String latestVersion = requestText(SPIGOT_UPDATE_URL);
        if (latestVersion.isEmpty()) return CheckResult.none();
        return CheckResult.of(latestVersion.trim(), isOutdated(latestVersion));
    }

    private String requestText(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", plugin.getName() + "/" + getCurrentVersion());

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) return "";

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            }
        } catch (Exception e) {
            if (plugin.getSettings().isDebugEnabled()) {
                plugin.getLogger().info("[Debug] Update check failed for " + url + ": " + e.getMessage());
            }
            return "";
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private boolean isOutdated(String latestVersion) {
        return compareVersions(getCurrentVersion(), latestVersion) < 0;
    }

    private int compareVersions(String currentVersion, String latestVersion) {
        String[] currentParts = normalizeVersion(currentVersion).split("\\.");
        String[] latestParts = normalizeVersion(latestVersion).split("\\.");
        int maxLength = Math.max(currentParts.length, latestParts.length);

        for (int i = 0; i < maxLength; i++) {
            int current = parseVersionPart(currentParts, i);
            int latest = parseVersionPart(latestParts, i);
            if (current != latest) return Integer.compare(current, latest);
        }

        return 0;
    }

    private int parseVersionPart(String[] parts, int index) {
        if (index >= parts.length) return 0;
        try {
            return Integer.parseInt(parts[index].replaceAll("[^0-9].*$", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String normalizeVersion(String version) {
        return version == null ? "" : version.trim().replaceFirst("^[vV]", "");
    }

    private String getCurrentVersion() {
        return plugin.getDescription().getVersion();
    }

    private static class CheckResult {
        private final String latestVersion;
        private final boolean updateAvailable;

        private CheckResult(String latestVersion, boolean updateAvailable) {
            this.latestVersion = latestVersion;
            this.updateAvailable = updateAvailable;
        }

        static CheckResult none() {
            return new CheckResult("", false);
        }

        static CheckResult of(String latestVersion, boolean updateAvailable) {
            return new CheckResult(latestVersion, updateAvailable);
        }

        boolean isUpdateAvailable() {
            return updateAvailable;
        }

        String getLatestVersion() {
            return latestVersion;
        }
    }
}
