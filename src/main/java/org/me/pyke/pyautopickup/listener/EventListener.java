package org.me.pyke.pyautopickup.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.me.pyke.pyautopickup.PyAutoPickup;
import org.me.pyke.pyautopickup.utils.Lang;
import org.me.pyke.pyautopickup.utils.Settings;

public class EventListener implements Listener {

    private final PyAutoPickup plugin;

    public EventListener(PyAutoPickup plugin) {
        this.plugin = plugin;
    }

    public void register() {
        HandlerList.unregisterAll(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        EventExecutor itemSpawnExecutor = (listener, event) -> {
            if (event instanceof ItemSpawnEvent) {
                ((EventListener) listener).onItemSpawn((ItemSpawnEvent) event);
            }
        };
        plugin.getServer().getPluginManager().registerEvent(
                ItemSpawnEvent.class,
                this,
                plugin.getSettings().getItemSpawnPriority(),
                itemSpawnExecutor,
                plugin,
                plugin.getSettings().isItemSpawnIgnoreCancelled()
        );
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

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
        if (!plugin.getSettings().isBlockPickupEnabled()) {
            debug("BlockBreak skipped: autopickup.blocks=false");
            return;
        }

        if (!hasPickupAccess(player)) {
            debug("BlockBreak skipped: missing " + Settings.USE_PERMISSION);
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE && !plugin.getSettings().isCreativePickupEnabled()) {
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

        if (!isPluginEnabled()) return;
        if (player == null) return;
        if (isWorldBlacklisted(player)) return;
        if (!plugin.getSettings().isMobDropPickupEnabled()) return;
        if (!hasPickupAccess(player)) return;

        if (player.getGameMode() == GameMode.CREATIVE && !plugin.getSettings().isCreativePickupEnabled()) {
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

        Player player = plugin.getDropOwnerManager().getDropOwner(location);
        debug("ItemSpawn item=" + item.getItemStack().getType()
                + " amount=" + item.getItemStack().getAmount()
                + " world=" + location.getWorld().getName()
                + " loc=" + formatLocation(location)
                + " cancelled=" + event.isCancelled()
                + " owner=" + (player == null ? "none" : player.getName())
                + " priority=" + plugin.getSettings().getItemSpawnPriority().name());

        if (player == null) return;
        if (isWorldBlacklisted(player)) {
            debug("ItemSpawn skipped: owner world is blacklisted");
            return;
        }
        if (!plugin.getToggleService().isPickupEnabled(player.getUniqueId())) {
            debug("ItemSpawn skipped: player toggle disabled");
            return;
        }
        if (!hasPickupAccess(player)) {
            debug("ItemSpawn skipped: missing " + Settings.USE_PERMISSION);
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE && !plugin.getSettings().isCreativePickupEnabled()) {
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

        String chatMessageFormat = Lang.get("full-inventory.chat-message-format", "Inventarul tau este plin! Itemele au fost dropate pe jos.");

        String titleMessageFormat = Lang.get("full-inventory.title-message-format", "FULL INVENTORY!");
        String subtitleMessageFormat = Lang.get("full-inventory.subtitle-message-format", "nu mai ai spatiu");

        if (plugin.getSettings().isFullInventoryChatEnabled()) {
            player.sendMessage(chatMessageFormat);
        }
        if (plugin.getSettings().isFullInventoryTitleEnabled()) {
            player.sendTitle(titleMessageFormat, subtitleMessageFormat, 10, 70, 20);
        }
    }

    private boolean isWorldBlacklisted(Player player) {
        return plugin.getSettings().isWorldBlacklisted(player.getWorld().getName());
    }

    private boolean isPluginEnabled() {
        return plugin.getSettings().isPluginEnabled();
    }

    private boolean isDebugEnabled() {
        return plugin.getSettings().isDebugEnabled();
    }

    private boolean hasPickupAccess(Player player) {
        return !plugin.getSettings().isPermissionRequired() || player.hasPermission(Settings.USE_PERMISSION);
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
