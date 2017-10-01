package org.border.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

final class ChatListener implements Listener
{
	Main plugin;

	ChatListener(Main p)
	{
		this.plugin = p;
	}

	@EventHandler
	private void onChat(AsyncPlayerChatEvent event)
	{
		if (this.plugin.getConfig().getBoolean("Settings.Chat.Chat_Channels.Enabled"))
		{
			ChatChannels(event);
		}
		if (this.plugin.getConfig().getBoolean("Settings.Chat.Message_Formatter.Enabled"))
		{
			MessageFormatter(event);
		}
	}

	private void MessageFormatter(AsyncPlayerChatEvent event)
	{
		char colorcode = this.plugin.getConfig().getString("Settings.Chat.Message_Formatter.Color_Character").charAt(0);
		Player p = event.getPlayer();
		ArrayList<String> prefix = Main.prefixes.get(p);
		ArrayList<String> suffix = Main.suffixes.get(p);
		String format = (String) Main.message_format.get(p);
		int prefix_length = 0;
		int suffix_length = 0;
		if (format == null)
		{
			format = ChatColor.translateAlternateColorCodes(colorcode, this.plugin.getConfig().getString("Settings.Chat.Message_Formatter.Default_Format"));
		}
		else
		{
			format = ChatColor.translateAlternateColorCodes(colorcode, format);
		}
		if (prefix != null)
		{
			ArrayList<String> prefix_new = new ArrayList<>();
			for (int i = 0; i < prefix.size(); i++)
			{
				String a = ChatColor.translateAlternateColorCodes(colorcode, (String) prefix.get(i));
				prefix_new.add(a);
			}
			prefix = prefix_new;
			prefix_length = prefix.size();
		}
		if (suffix != null)
		{
			ArrayList<String> suffix_new = new ArrayList<>();
			for (int i = 0; i < suffix.size(); i++)
			{
				String a = ChatColor.translateAlternateColorCodes(colorcode, (String) suffix.get(i));
				suffix_new.add(a);
			}
			suffix = suffix_new;
			suffix_length = suffix.size();
		}
		StringBuffer msg = new StringBuffer();
		char[] formatarray = format.toCharArray();
		int formatlength = formatarray.length;
		for (int i = 0; i < formatlength; i++)
		{
			char a = formatarray[i];
			if (a == '%')
			{
				if (i + 1 >= formatlength)
				{
					msg.append(a);
					break;
				}
				i++;
				switch (formatarray[i])
				{
				case 'p':
					if (i + 1 >= formatlength)
					{
						if (prefix_length >= 1)
						{
							msg.append((String) prefix.get(0));
						}
					}
					else
					{
						i++;
						switch (formatarray[i])
						{
						case '1':
							if (prefix_length < 1)
							{
								continue;
							}
							msg.append((String) prefix.get(0));
							break;
						case '2':
							if (prefix_length < 2)
							{
								continue;
							}
							msg.append((String) prefix.get(1));
							break;
						case '3':
							if (prefix_length < 3)
							{
								continue;
							}
							msg.append((String) prefix.get(2));
							break;
						case '4':
							if (prefix_length < 4)
							{
								continue;
							}
							msg.append((String) prefix.get(3));
							break;
						case '5':
							if (prefix_length < 5)
							{
								continue;
							}
							msg.append((String) prefix.get(4));
							break;
						case '6':
							if (prefix_length < 6)
							{
								continue;
							}
							msg.append((String) prefix.get(5));
							break;
						case '7':
							if (prefix_length < 7)
							{
								continue;
							}
							msg.append((String) prefix.get(6));
							break;
						case '8':
							if (prefix_length < 8)
							{
								continue;
							}
							msg.append((String) prefix.get(7));
							break;
						case '9':
							if (prefix_length < 9)
							{
								continue;
							}
							msg.append((String) prefix.get(8));
							break;
						default:
							if (prefix_length >= 1)
							{
								msg.append((String) prefix.get(0));
							}
							i--;
						}
					}
					break;
				case 's':
					if (i + 1 >= formatlength)
					{
						if (suffix_length >= 1)
						{
							msg.append((String) suffix.get(0));
						}
					}
					else
					{
						i++;
						switch (formatarray[i])
						{
						case '1':
							if (suffix_length < 1)
							{
								continue;
							}
							msg.append((String) suffix.get(0));
							break;
						case '2':
							if (suffix_length < 2)
							{
								continue;
							}
							msg.append((String) suffix.get(1));
							break;
						case '3':
							if (suffix_length < 3)
							{
								continue;
							}
							msg.append((String) suffix.get(2));
							break;
						case '4':
							if (suffix_length < 4)
							{
								continue;
							}
							msg.append((String) suffix.get(3));
							break;
						case '5':
							if (suffix_length < 5)
							{
								continue;
							}
							msg.append((String) suffix.get(4));
							break;
						case '6':
							if (suffix_length < 6)
							{
								continue;
							}
							msg.append((String) suffix.get(5));
							break;
						case '7':
							if (suffix_length < 7)
							{
								continue;
							}
							msg.append((String) suffix.get(6));
							break;
						case '8':
							if (suffix_length < 8)
							{
								continue;
							}
							msg.append((String) suffix.get(7));
							break;
						case '9':
							if (suffix_length < 9)
							{
								continue;
							}
							msg.append((String) suffix.get(8));
							break;
						default:
							if (suffix_length >= 1)
							{
								msg.append((String) suffix.get(0));
							}
							i--;
						}
					}
					break;
				case 'n':
					msg.append(p.getName());
					break;
				case 'd':
					msg.append(p.getDisplayName());
					break;
				case 'm':
					msg.append(event.getMessage());
					break;
				case 'w':
					String world = Main.worldaliases.get(p.getLocation().getWorld().getName());
					if (world == null) world = p.getLocation().getWorld().getName();
					msg.append(world);
					break;
				default:
					msg.append(a);
					i--;

					break;
				}
			}
			else
			{
				msg.append(a);
			}
		}
		event.setFormat(msg.toString().replace("%", "%%"));
	}

	private void ChatChannels(AsyncPlayerChatEvent event)
	{
		Set<Player> r = event.getRecipients();
		Set<String> send = Main.chat_send_enabled.get(event.getPlayer());
		if (send == null) { return; }
		for (Player p : r)
		{
			Set<String> receive = Main.chat_receive_enabled.get(p);
			if ((receive != null) && (Collections.disjoint(send, receive)))
			{
				event.getRecipients().remove(p);
			}
		}
	}
}
