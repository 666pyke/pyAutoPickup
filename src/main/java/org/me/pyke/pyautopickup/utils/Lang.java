package org.me.pyke.pyautopickup.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Lang {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static FileConfiguration cfg;

    private Lang() {}

    public static void init(FileConfiguration config) {
        cfg = config;
    }

    public static String color(String s) {
        if (s == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(s);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexColor = "#" + matcher.group(1);
            matcher.appendReplacement(buffer, org.bukkit.ChatColor.COLOR_CHAR + "x"
                    + org.bukkit.ChatColor.COLOR_CHAR + hexColor.substring(1, 2)
                    + org.bukkit.ChatColor.COLOR_CHAR + hexColor.substring(2, 3)
                    + org.bukkit.ChatColor.COLOR_CHAR + hexColor.substring(3, 4)
                    + org.bukkit.ChatColor.COLOR_CHAR + hexColor.substring(4, 5)
                    + org.bukkit.ChatColor.COLOR_CHAR + hexColor.substring(5, 6)
                    + org.bukkit.ChatColor.COLOR_CHAR + hexColor.substring(6, 7));
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String get(String path) {
        return get(path, "");
    }

    public static String get(String path, String def) {
        if (cfg == null) return color(def);
        return color(cfg.getString(path, def));
    }

    public static List<String> getList(String path) {
        if (cfg == null || !cfg.isList(path)) return Collections.emptyList();
        return cfg.getStringList(path).stream().map(Lang::color).collect(Collectors.toList());
    }

    public static String get(String path, Map<String, String> ph) {
        return applyPlaceholders(get(path, ""), ph);
    }

    public static List<String> getList(String path, Map<String, String> ph) {
        return getList(path).stream().map(s -> applyPlaceholders(s, ph)).collect(Collectors.toList());
    }

    public static String prefixed(String path, String def) {
        return get("messages.prefix", "") + get(path, def);
    }

    public static String prefixedRaw(String rawColored) {
        return get("messages.prefix", "") + color(rawColored);
    }

    public static List<String> prefixedList(String path) {
        String prefix = get("messages.prefix", "");
        return getList(path).stream().map(s -> prefix + s).collect(Collectors.toList());
    }

    private static String applyPlaceholders(String s, Map<String, String> ph) {
        if (ph == null || ph.isEmpty()) return s;
        for (Map.Entry<String, String> e : ph.entrySet()) s = s.replace(e.getKey(), e.getValue());
        return s;
    }
}
