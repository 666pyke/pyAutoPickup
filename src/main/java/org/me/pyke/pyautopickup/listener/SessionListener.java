
package org.me.pyke.pyautopickup.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.me.pyke.pyautopickup.PyAutoPickup;

public class SessionListener implements Listener {
    private final PyAutoPickup plugin;
    public SessionListener(PyAutoPickup plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getToggleService().warmup(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getToggleService().remove(e.getPlayer().getUniqueId());
    }

}
