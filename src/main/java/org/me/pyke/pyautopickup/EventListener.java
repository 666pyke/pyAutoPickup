package org.me.pyke.pyautopickup;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {

    private final PyAutoPickup plugin;

    public EventListener(PyAutoPickup plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        if (!config.getBoolean("plugin-enabled", true) || !config.getBoolean("autopickup.blocks", true)) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE && !config.getBoolean("autopickup.works-in-creative", false)) {
            return;
        }

        plugin.getDropOwnerManager().register(player, event.getBlock().getLocation());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        FileConfiguration config = plugin.getConfig();

        if (player != null && config.getBoolean("autopickup.mob-drops", true)) {
            if (player.getGameMode() == GameMode.CREATIVE && !config.getBoolean("autopickup.works-in-creative", false)) {
                return;
            }

            plugin.getDropOwnerManager().register(player, event.getEntity().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Location location = item.getLocation();
        FileConfiguration config = plugin.getConfig();

        Player player = plugin.getDropOwnerManager().getDropOwner(location);

        if (player == null) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE && !config.getBoolean("autopickup.works-in-creative", false)) {
            return;
        }

        ItemStack itemStack = item.getItemStack();

        if (player.getInventory().addItem(itemStack).isEmpty()) {
            event.setCancelled(true);
        } else {
            sendFullInventoryMessage(player);
        }
    }

    private void sendFullInventoryMessage(Player player) {
        FileConfiguration config = plugin.getConfig();
        boolean showChatMessage = config.getBoolean("full-inventory.chat-message", true);
        String chatMessageFormat = ChatColor.translateAlternateColorCodes('&', config.getString("full-inventory.chat-message-format", "Inventarul tău este plin! Itemele au fost dropate pe jos."));

        boolean showTitleMessage = config.getBoolean("full-inventory.title-message", true);
        String titleMessageFormat = ChatColor.translateAlternateColorCodes('&', config.getString("full-inventory.title-message-format", "FULL INVENTORY!"));
        String subtitleMessageFormat = ChatColor.translateAlternateColorCodes('&', config.getString("full-inventory.subtitle-message-format", "nu mai ai spatiu"));

        if (showChatMessage) {
            player.sendMessage(chatMessageFormat);
        }
        if (showTitleMessage) {
            player.sendTitle(titleMessageFormat, subtitleMessageFormat, 10, 70, 20);
        }
    }
}
