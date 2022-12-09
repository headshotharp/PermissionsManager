package org.border.permission;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

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
        char colorcode = plugin.getConfig().getString("Settings.Chat.Message_Formatter.Color_Character").charAt(0);
        Player p = event.getPlayer();
        ArrayList<String> prefix = Main.prefixes.get(p);
        ArrayList<String> suffix = Main.suffixes.get(p);
        String format = Main.messageFormat.get(p);
        int prefixLength = 0;
        int suffixLength = 0;
        if (format == null) {
            format = ChatColor.translateAlternateColorCodes(colorcode,
                    plugin.getConfig().getString("Settings.Chat.Message_Formatter.Default_Format"));
        } else {
            format = ChatColor.translateAlternateColorCodes(colorcode, format);
        }
        if (prefix != null) {
            ArrayList<String> prefix_new = new ArrayList<>();
            for (int i = 0; i < prefix.size(); i++) {
                String a = ChatColor.translateAlternateColorCodes(colorcode, prefix.get(i));
                prefix_new.add(a);
            }
            prefix = prefix_new;
            prefixLength = prefix.size();
        }
        if (suffix != null) {
            ArrayList<String> suffix_new = new ArrayList<>();
            for (int i = 0; i < suffix.size(); i++) {
                String a = ChatColor.translateAlternateColorCodes(colorcode, suffix.get(i));
                suffix_new.add(a);
            }
            suffix = suffix_new;
            suffixLength = suffix.size();
        }
        StringBuffer msg = new StringBuffer();
        char[] formatarray = format.toCharArray();
        int formatlength = formatarray.length;
        for (int i = 0; i < formatlength; i++) {
            char a = formatarray[i];
            if (a == '%') {
                if (i + 1 >= formatlength) {
                    msg.append(a);
                    break;
                }
                i++;
                switch (formatarray[i]) {
                case 'p':
                    if (i + 1 >= formatlength) {
                        if (prefixLength >= 1) {
                            msg.append(prefix.get(0));
                        }
                    } else {
                        i++;
                        switch (formatarray[i]) {
                        case '1':
                            if (prefixLength < 1) {
                                continue;
                            }
                            msg.append(prefix.get(0));
                            break;
                        case '2':
                            if (prefixLength < 2) {
                                continue;
                            }
                            msg.append(prefix.get(1));
                            break;
                        case '3':
                            if (prefixLength < 3) {
                                continue;
                            }
                            msg.append(prefix.get(2));
                            break;
                        case '4':
                            if (prefixLength < 4) {
                                continue;
                            }
                            msg.append(prefix.get(3));
                            break;
                        case '5':
                            if (prefixLength < 5) {
                                continue;
                            }
                            msg.append(prefix.get(4));
                            break;
                        case '6':
                            if (prefixLength < 6) {
                                continue;
                            }
                            msg.append(prefix.get(5));
                            break;
                        case '7':
                            if (prefixLength < 7) {
                                continue;
                            }
                            msg.append(prefix.get(6));
                            break;
                        case '8':
                            if (prefixLength < 8) {
                                continue;
                            }
                            msg.append(prefix.get(7));
                            break;
                        case '9':
                            if (prefixLength < 9) {
                                continue;
                            }
                            msg.append(prefix.get(8));
                            break;
                        default:
                            if (prefixLength >= 1) {
                                msg.append(prefix.get(0));
                            }
                            i--;
                        }
                    }
                    break;
                case 's':
                    if (i + 1 >= formatlength) {
                        if (suffixLength >= 1) {
                            msg.append(suffix.get(0));
                        }
                    } else {
                        i++;
                        switch (formatarray[i]) {
                        case '1':
                            if (suffixLength < 1) {
                                continue;
                            }
                            msg.append(suffix.get(0));
                            break;
                        case '2':
                            if (suffixLength < 2) {
                                continue;
                            }
                            msg.append(suffix.get(1));
                            break;
                        case '3':
                            if (suffixLength < 3) {
                                continue;
                            }
                            msg.append(suffix.get(2));
                            break;
                        case '4':
                            if (suffixLength < 4) {
                                continue;
                            }
                            msg.append(suffix.get(3));
                            break;
                        case '5':
                            if (suffixLength < 5) {
                                continue;
                            }
                            msg.append(suffix.get(4));
                            break;
                        case '6':
                            if (suffixLength < 6) {
                                continue;
                            }
                            msg.append(suffix.get(5));
                            break;
                        case '7':
                            if (suffixLength < 7) {
                                continue;
                            }
                            msg.append(suffix.get(6));
                            break;
                        case '8':
                            if (suffixLength < 8) {
                                continue;
                            }
                            msg.append(suffix.get(7));
                            break;
                        case '9':
                            if (suffixLength < 9) {
                                continue;
                            }
                            msg.append(suffix.get(8));
                            break;
                        default:
                            if (suffixLength >= 1) {
                                msg.append(suffix.get(0));
                            }
                            i--;
                        }
                    }
                    break;
                case 'n':
                    msg.append(p.getName());
                    break;
                case 'd':
                    if (p.displayName() instanceof TextComponent text) {
                        msg.append(text.content());
                    }
                    break;
                case 'm':
                    if (event.message() instanceof TextComponent text) {
                        msg.append(text.content());
                    }
                    break;
                case 'w':
                    String world = Main.worldaliases.get(p.getLocation().getWorld().getName());
                    if (world == null) {
                        world = p.getLocation().getWorld().getName();
                    }
                    msg.append(world);
                    break;
                default:
                    msg.append(a);
                    i--;
                    break;
                }
            } else {
                msg.append(a);
            }
        }
        event.message(Component.empty().content(msg.toString().replace("%", "%%")));
    }
}
