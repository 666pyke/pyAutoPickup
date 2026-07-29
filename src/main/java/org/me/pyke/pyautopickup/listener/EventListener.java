package org.me.pyke.pyautopickup.listener;

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
import org.bukkit.plugin.EventExecutor;
import org.me.pyke.pyautopickup.PyAutoPickup;

public class EventListener implements Listener {

    private final PyAutoPickup plugin;
    private final EventPriority itemSpawnPriority;

    public EventListener(PyAutoPickup plugin, EventPriority itemSpawnPriority) {
        this.plugin = plugin;
        this.itemSpawnPriority = itemSpawnPriority;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        EventExecutor itemSpawnExecutor = (listener, event) -> {
            if (event instanceof ItemSpawnEvent) {
                ((EventListener) listener).onItemSpawn((ItemSpawnEvent) event);
            }
        };
        plugin.getServer().getPluginManager().registerEvent(
                ItemSpawnEvent.class,
                this,
                itemSpawnPriority,
                itemSpawnExecutor,
                plugin,
                plugin.getConfig().getBoolean("advanced.item-spawn-ignore-cancelled", true)
        );
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        debug("BlockBreak block=" + event.getBlock().getType()
                + " player=" + player.getName()
                + " world=" + player.getWorld().getName()
                + " cancelled=" + event.isCancelled());

        if (!isPluginEnabled()) {
            debug("BlockBreak skipped: plugin-enabled=false");
            return;
        }
        if (isWorldBlacklisted(player)) {
            debug("BlockBreak skipped: world is blacklisted");
            return;
        }
        if (!config.getBoolean("autopickup.blocks", true)) {
            debug("BlockBreak skipped: autopickup.blocks=false");
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE && !config.getBoolean("autopickup.works-in-creative", false)) {
            debug("BlockBreak skipped: creative disabled");
            return;
        }

        if (!plugin.getToggleService().isPickupEnabled(player.getUniqueId())) {
            debug("BlockBreak skipped: player toggle disabled");
            return;
        }

        plugin.getDropOwnerManager().register(player, event.getBlock().getLocation(), event.getBlock());
        debug("BlockBreak registered owner for " + player.getName() + " at " + formatLocation(event.getBlock().getLocation()));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        FileConfiguration config = plugin.getConfig();

        if (!isPluginEnabled()) return;
        if (player == null) return;
        if (isWorldBlacklisted(player)) return;
        if (!config.getBoolean("autopickup.mob-drops", true)) return;

        if (player.getGameMode() == GameMode.CREATIVE && !config.getBoolean("autopickup.works-in-creative", false)) {
            return;
        }

        if (!plugin.getToggleService().isPickupEnabled(player.getUniqueId())) return;

        plugin.getDropOwnerManager().register(player, event.getEntity().getLocation());
    }

    public void onItemSpawn(ItemSpawnEvent event) {
        if (!isPluginEnabled()) {
            debug("ItemSpawn skipped: plugin-enabled=false");
            return;
        }

        Item item = event.getEntity();
        Location location = item.getLocation();
        FileConfiguration config = plugin.getConfig();

        Player player = plugin.getDropOwnerManager().getDropOwner(location);
        debug("ItemSpawn item=" + item.getItemStack().getType()
                + " amount=" + item.getItemStack().getAmount()
                + " world=" + location.getWorld().getName()
                + " loc=" + formatLocation(location)
                + " cancelled=" + event.isCancelled()
                + " owner=" + (player == null ? "none" : player.getName())
                + " priority=" + itemSpawnPriority.name());

        if (player == null) return;
        if (isWorldBlacklisted(player)) {
            debug("ItemSpawn skipped: owner world is blacklisted");
            return;
        }
        if (!plugin.getToggleService().isPickupEnabled(player.getUniqueId())) {
            debug("ItemSpawn skipped: player toggle disabled");
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE && !config.getBoolean("autopickup.works-in-creative", false)) {
            debug("ItemSpawn skipped: creative disabled");
            return;
        }

        ItemStack itemStack = item.getItemStack();

        if (player.getInventory().addItem(itemStack).isEmpty()) {
            event.setCancelled(true);
            debug("ItemSpawn collected into " + player.getName() + "'s inventory");
        } else {
            debug("ItemSpawn inventory full for " + player.getName());
            sendFullInventoryMessage(player);
        }
    }

    private void sendFullInventoryMessage(Player player) {
        if (!plugin.getToggleService().isNotifyEnabled(player.getUniqueId())) return;

        FileConfiguration config = plugin.getConfig();
        boolean showChatMessage = config.getBoolean("full-inventory.chat-message", true);
        String chatMessageFormat = ChatColor.translateAlternateColorCodes(
                '&',
                config.getString("full-inventory.chat-message-format", "Inventarul tau este plin! Itemele au fost dropate pe jos.")
        );

        boolean showTitleMessage = config.getBoolean("full-inventory.title-message", true);
        String titleMessageFormat = ChatColor.translateAlternateColorCodes(
                '&',
                config.getString("full-inventory.title-message-format", "FULL INVENTORY!")
        );
        String subtitleMessageFormat = ChatColor.translateAlternateColorCodes(
                '&',
                config.getString("full-inventory.subtitle-message-format", "nu mai ai spatiu")
        );

        if (showChatMessage) {
            player.sendMessage(chatMessageFormat);
        }
        if (showTitleMessage) {
            player.sendTitle(titleMessageFormat, subtitleMessageFormat, 10, 70, 20);
        }
    }

    private boolean isWorldBlacklisted(Player player) {
        return plugin.getConfig()
                .getStringList("blacklisted-worlds")
                .contains(player.getWorld().getName());
    }

    private boolean isPluginEnabled() {
        return plugin.getConfig().getBoolean("plugin-enabled", true);
    }

    private boolean isDebugEnabled() {
        return plugin.getConfig().getBoolean("advanced.debug", false);
    }

    private void debug(String message) {
        if (isDebugEnabled()) {
            plugin.getLogger().info("[Debug] " + message);
        }
    }

    private String formatLocation(Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
}
