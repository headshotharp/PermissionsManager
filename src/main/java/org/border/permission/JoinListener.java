package org.border.permission;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

    private Main plugin;

    protected JoinListener(Main p) {
        plugin = p;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (plugin.getServer().getOnlineMode()) {
            plugin.checkUuid(p);
        }
        plugin.setPrefixes(p);
        plugin.setPermissions(p);
        if ((p.hasPermission("pm.update")) && (Main.update)) {
            p.sendMessage(ChatColor.RED + "An update for Permission Manager has been found!");
        }
    }

    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        plugin.setPermissions(p);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        Main.permissions.get(p).remove();
        Main.permissions.remove(p);
        Main.prefixes.remove(p);
        Main.suffixes.remove(p);
        Main.messageFormat.remove(p);
        Main.chatReceiveAll.remove(p);
        Main.chatSendAll.remove(p);
        Main.chatReceiveEnabled.remove(p);
        Main.chatSendEnabled.remove(p);
    }
}
