package org.border.permission;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

class ChatListener implements Listener {

    private Main plugin;

    protected ChatListener(Main p) {
        plugin = p;
    }

    @EventHandler
    private void onChat(AsyncChatEvent event) {
        if (plugin.getConfig().getBoolean("Settings.Chat.Message_Formatter.Enabled")) {
            messageFormatter(event);
        }
    }

    private void messageFormatter(AsyncChatEvent event) {
        Player p = event.getPlayer();
        String world = Main.worldaliases.get(p.getLocation().getWorld().getName());
        if (world == null) {
            world = p.getLocation().getWorld().getName();
        }
        Component namePrefix = text(Main.prefixes.getOrDefault(p, Arrays.asList("")).get(0));
        Component message = Component.join(JoinConfiguration.noSeparators(), //
                text("[" + world + "]"), //
                namePrefix, //
                text(" "), //
                p.displayName(), //
                text(": "), //
                event.message() //
        );
        plugin.getServer().sendMessage(message);
        event.setCancelled(true);
    }

    private TextComponent text(String s) {
        TextColor c = NamedTextColor.WHITE;
        for (String color : NamedTextColor.NAMES.keys()) {
            String format = "%" + color + "%";
            if (s.startsWith(format)) {
                c = NamedTextColor.NAMES.value(color);
                s = s.replace(format, "");
                break;
            }
        }
        return text(c, s);
    }

    private TextComponent text(TextColor color, String s) {
        return Component.empty().color(color).content(s);
    }
}
