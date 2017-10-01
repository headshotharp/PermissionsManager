package org.border.permission;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

final class JoinListener implements Listener {
	Main plugin;

	JoinListener(Main p) {
		this.plugin = p;
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (this.plugin.getServer().getOnlineMode()) {
			this.plugin.checkUuid(p);
		}
		this.plugin.setPrefixes(p);
		this.plugin.setPermissions(p);
		if ((p.hasPermission("pm.update")) && (Main.update)) {
			p.sendMessage(ChatColor.RED + "An update for Permission Manager has been found!");
		}
	}

	@EventHandler
	private void onWorldChange(PlayerChangedWorldEvent event) {
		Player p = event.getPlayer();
		this.plugin.setPermissions(p);
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		((PermissionAttachment) Main.permissions.get(p)).remove();
		Main.permissions.remove(p);
		Main.prefixes.remove(p);
		Main.suffixes.remove(p);
		Main.message_format.remove(p);
		Main.chat_receive_all.remove(p);
		Main.chat_send_all.remove(p);
		Main.chat_receive_enabled.remove(p);
		Main.chat_send_enabled.remove(p);
	}
}
