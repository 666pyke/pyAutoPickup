package org.me.pyke.pyautopickup;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.me.pyke.pyautopickup.utils.Lang;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final PyAutoPickup plugin;

    public CommandManager(PyAutoPickup plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // Help (listă)
        if (args.length == 0) {
            for (String line : Lang.prefixedList("messages.help")) {
                sender.sendMessage(line);
            }
            return true;
        }

        // /pyautopickup reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (p.hasPermission("pyautopickup.reload")) {
                    plugin.reloadPluginConfig();
                    p.sendMessage(Lang.prefixed("messages.reload-success", "&aConfig reloaded!"));
                } else {
                    p.sendMessage(Lang.prefixed("messages.no-permission", "&cYou do not have permission to use this command."));
                }
            } else {
                plugin.reloadPluginConfig();
                sender.sendMessage(Lang.prefixed("messages.reload-success", "&aConfig reloaded!"));
            }
            return true;
        }

        // /pyautopickup toggle
        if (args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Lang.prefixed("messages.only-player", "&cOnly players can use this command."));
                return true;
            }
            Player p = (Player) sender;
            boolean on = plugin.getToggleService().togglePickup(p.getUniqueId());
            p.sendMessage(on
                    ? Lang.prefixed("messages.toggle-on", "&aPyAutoPickup &fis &aENABLED &ffor you.")
                    : Lang.prefixed("messages.toggle-off", "&aPyAutoPickup &fis &cDISABLED &ffor you."));
            return true;
        }

        // /pyautopickup msgtoggle
        if (args[0].equalsIgnoreCase("msgtoggle")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Lang.prefixed("messages.only-player", "&cOnly players can use this command."));
                return true;
            }
            Player p = (Player) sender;
            boolean on = plugin.getToggleService().toggleNotify(p.getUniqueId());
            p.sendMessage(on
                    ? Lang.prefixed("messages.msgtoggle-on", "&7Full-inventory messages are &aENABLED&7.")
                    : Lang.prefixed("messages.msgtoggle-off", "&7Full-inventory messages are &cDISABLED&7."));
            return true;
        }

        sender.sendMessage(Lang.prefixed("messages.unknown-subcommand", "&cUnknown subcommand. Use: /pyautopickup"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> c = new ArrayList<String>();
        if (args.length == 1) {
            String a = args[0].toLowerCase();
            if ("toggle".startsWith(a)) c.add("toggle");
            if ("msgtoggle".startsWith(a)) c.add("msgtoggle");
            if ("reload".startsWith(a)) c.add("reload");
        }
        return c;
    }
}
