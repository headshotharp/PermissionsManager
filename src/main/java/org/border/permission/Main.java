package org.border.permission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class Main extends JavaPlugin {
	private static FileConfiguration permissionsfile;
	private static FileConfiguration uuidfile;
	private static File permissionsDatafile;
	private static File uuidDatafile;
	private static List<String> defaultGroup = new ArrayList<>();
	private final ChatListener chatlistener = new ChatListener(this);
	private static boolean listenerOn = false;
	private static boolean debug = false;
	static boolean update = false;
	static Set<Permission> permissionsRegistered;
	static Map<String, Map<Integer, String>> rankMapGroup = new HashMap<>();
	static Map<String, List<Integer>> rankMapRank = new HashMap<>();
	static Map<Player, PermissionAttachment> permissions = new HashMap<>();
	static Map<Player, ArrayList<String>> prefixes = new HashMap<>();
	static Map<Player, ArrayList<String>> suffixes = new HashMap<>();
	static Map<Player, String> messageFormat = new HashMap<>();
	static Map<Player, Set<String>> chatSendAll = new HashMap<>();
	static Map<Player, Set<String>> chatReceiveAll = new HashMap<>();
	static Map<Player, Set<String>> chatSendEnabled = new HashMap<>();
	static Map<Player, Set<String>> chatReceiveEnabled = new HashMap<>();
	static Map<String, String> worldaliases = new HashMap<>();
	static boolean wildcard;
	static boolean extendedDebug;

	@Override
	public void onEnable() {
		createFiles();
		loadFiles();
		setDefaults();
		setDefaultGroup();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JoinListener(this), this);
		if (((getConfig().getBoolean("Settings.Chat.Message_Formatter.Enabled"))
				|| (getConfig().getBoolean("Settings.Chat.Chat_Channels.Enabled"))) && (!listenerOn)) {
			pm.registerEvents(this.chatlistener, this);
			listenerOn = true;
		}
		Set<String> keys = uuidfile.getKeys(false);
		boolean save = false;
		for (Iterator<String> localIterator = keys.iterator(); localIterator.hasNext(); save = true) {
			String a = localIterator.next();
			String b = uuidfile.getString(a);
			if ((b == null) || (permissionsfile.get("users." + b) != null)) {
				break;
			}
			uuidfile.set(a, null);
		}
		if (save) {
			saveUuidFile();
		}
		if (!getServer().getOnlinePlayers().isEmpty()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Player p : getServer().getOnlinePlayers()) {
						setPrefixes(p);
						setPermissions(p);
					}
				}
			}.runTaskLater(this, 1L);
		}
		worldaliases = new HashMap<>();
		ConfigurationSection configSection = getConfig().getConfigurationSection("Worldaliases");
		if (configSection != null) {
			for (String s : getConfig().getConfigurationSection("Worldaliases").getKeys(false)) {
				worldaliases.put(s, getConfig().getString("Worldaliases." + s));
			}
		}
	}

	@Override
	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			permissions.get(p).remove();
		}
	}

	private void createFiles() {
		permissionsDatafile = new File(getDataFolder() + "/permissions.yml");
		uuidDatafile = new File(getDataFolder() + "/uuid.yml");
		if (!permissionsDatafile.exists()) {
			try {
				permissionsDatafile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadFiles() {
		permissionsfile = YamlConfiguration.loadConfiguration(permissionsDatafile);
		uuidfile = YamlConfiguration.loadConfiguration(uuidDatafile);
	}

	private void setDefaults() {
		FileConfiguration config = getConfig();
		boolean save = false;
		if (config.get("Settings.General.Updater.enabled") == null) {
			config.set("Settings.General.Updater.enabled", Boolean.valueOf(true));
			save = true;
		}
		if (config.get("Settings.Permissions.Wildcard.Use") == null) {
			config.set("Settings.Permissions.Wildcard.Use", Boolean.valueOf(false));
			save = true;
		}
		if (config.get("Settings.Permissions.Wildcard.Extended_Debug") == null) {
			config.set("Settings.Permissions.Wildcard.Extended_Debug", Boolean.valueOf(false));
			save = true;
		}
		if (config.get("Settings.Chat.Message_Formatter.Enabled") == null) {
			config.set("Settings.Chat.Message_Formatter.Enabled", Boolean.valueOf(false));
			save = true;
		}
		if (config.get("Settings.Chat.Message_Formatter.Default_Format") == null) {
			config.set("Settings.Chat.Message_Formatter.Default_Format", "%p1 <%d> %m %s1");
			save = true;
		}
		if (config.get("Settings.Chat.Message_Formatter.Color_Character") == null) {
			config.set("Settings.Chat.Message_Formatter.Color_Character", "&");
			save = true;
		}
		if (config.get("Settings.Chat.Chat_Channels.Enabled") == null) {
			config.set("Settings.Chat.Chat_Channels.Enabled", Boolean.valueOf(false));
			save = true;
		}
		if (config.get("Worldaliases.world") == null) {
			config.set("Worldaliases.world", "W");
			save = true;
		}
		if (save) {
			saveConfig();
		}
	}

	private void savePermissionFile() {
		try {
			permissionsfile.save(permissionsDatafile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveUuidFile() {
		try {
			uuidfile.save(uuidDatafile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void reload() {
		createFiles();
		loadFiles();
		reloadConfig();
		setDefaults();
		setDefaultGroup();
		for (Player p : getServer().getOnlinePlayers()) {
			setPrefixes(p);
			setPermissions(p);
		}
		if (((getConfig().getBoolean("Settings.Chat.Message_Formatter.Enabled"))
				|| (getConfig().getBoolean("Settings.Chat.Chat_Channels.Enabled"))) && (!listenerOn)) {
			getServer().getPluginManager().registerEvents(this.chatlistener, this);
			listenerOn = true;
		}
		if ((!getConfig().getBoolean("Settings.Chat.Message_Formatter.Enabled"))
				&& (!getConfig().getBoolean("Settings.Chat.Chat_Channels.Enabled")) && (listenerOn)) {
			HandlerList.unregisterAll(this.chatlistener);
			listenerOn = false;
		}
	}

	private void setDefaultGroup() {
		defaultGroup.clear();
		rankMapRank.clear();
		rankMapGroup.clear();
		wildcard = getConfig().getBoolean("Settings.Permissions.Wildcard.Use");
		extendedDebug = getConfig().getBoolean("Settings.Permissions.Wildcard.Extended_Debug");
		if (permissionsfile.get("groups") == null) {
			return;
		}
		for (String group : permissionsfile.getConfigurationSection("groups").getKeys(false)) {
			if (permissionsfile.getBoolean("groups." + group + ".default")) {
				defaultGroup.add(group);
			}
			if (permissionsfile.get("groups." + group + ".options.rank") != null) {
				int rank = permissionsfile.getInt("groups." + group + ".options.rank");

				String ladder = permissionsfile.getString("groups." + group + ".options.rank-ladder", "Default");
				Map<Integer, String> a = rankMapGroup.get(ladder);
				List<Integer> b = rankMapRank.get(ladder);
				if (a == null) {
					a = new HashMap<>();
				}
				a.put(Integer.valueOf(rank), group);
				if (b == null) {
					b = new ArrayList<>();
				}
				b.add(Integer.valueOf(rank));
				rankMapGroup.put(ladder, a);
				rankMapRank.put(ladder, b);
			}
		}
		for (List<Integer> a : rankMapRank.values()) {
			Collections.sort(a);
		}
	}

	void calculateGroupInheritance(PermissionAttachment pa, List<String> list, String w) {
		int size = list.size() - 1;
		for (int i = size; i >= 0; i--) {
			String group = list.get(i);
			calculateGroupInheritance(pa, permissionsfile.getStringList("groups." + group + ".inheritance"), w);
			applyPermissions(pa, permissionsfile.getStringList("groups." + group + ".permissions"));
			calculateWorldGroupInheritance(pa,
					permissionsfile.getStringList("groups." + group + ".worlds." + w + ".inheritance"), group);
			applyPermissions(pa, permissionsfile.getStringList("groups." + group + ".worlds." + w + ".permissions"));
		}
	}

	void calculateWorldGroupInheritance(PermissionAttachment pa, List<String> list, String g) {
		int size = list.size() - 1;
		for (int i = size; i >= 0; i--) {
			String world = list.get(i);
			calculateWorldGroupInheritance(pa,
					permissionsfile.getStringList("groups." + g + ".worlds." + world + ".inheritance"), g);
			applyPermissions(pa, permissionsfile.getStringList("groups." + g + ".worlds." + world + ".permissions"));
		}
	}

	void calculateWorldUserInheritance(PermissionAttachment pa, List<String> list, String u) {
		int size = list.size() - 1;
		for (int i = size; i >= 0; i--) {
			String world = list.get(i);
			calculateWorldUserInheritance(pa,
					permissionsfile.getStringList("users." + u + ".worlds." + world + ".inheritance"), u);
			applyPermissions(pa, permissionsfile.getStringList("users." + u + ".worlds." + world + ".permissions"));
		}
	}

	void setPrefixes(Player p) {
		String name = p.getName();
		ArrayList<String> prefix = new ArrayList<>();
		ArrayList<String> suffix = new ArrayList<>();

		List<String> grouplist = permissionsfile.getStringList("users." + name + ".group");
		if (grouplist.isEmpty()) {
			grouplist.addAll(defaultGroup);
		}
		String user_prefix = permissionsfile.getString("users." + name + ".prefix");
		if (user_prefix != null) {
			prefix.add(user_prefix);
		}
		String user_suffix = permissionsfile.getString("users." + name + ".suffix");
		if (user_suffix != null) {
			suffix.add(user_suffix);
		}
		String messageformat = permissionsfile.getString("users." + name + ".message_format");
		String chat_send = permissionsfile.getString("users." + name + ".options.chat-send");
		String chat_receive = permissionsfile.getString("users." + name + ".options.chat-receive");
		String group;
		String group_suffix;
		label474: for (Iterator<String> localIterator = grouplist.iterator(); localIterator
				.hasNext(); chat_receive = permissionsfile.getString("groups." + group + ".options.chat-receive")) {
			group = localIterator.next();
			String group_prefix = permissionsfile.getString("groups." + group + ".prefix");
			if (group_prefix != null) {
				prefix.add(group_prefix);
			}
			group_suffix = permissionsfile.getString("groups." + group + ".suffix");
			if (group_suffix != null) {
				suffix.add(group_suffix);
			}
			if (messageformat == null) {
				messageformat = permissionsfile.getString("groups." + group + ".message_format");
			}
			if ((chat_send != null) || (chat_receive != null)) {
				break label474;
			}
			chat_send = permissionsfile.getString("groups." + group + ".options.chat-send");
		}
		String[] arrayOfString;
		if (chat_send != null) {
			Set<String> all = new HashSet<>();
			Set<String> enabled = new HashSet<>();
			arrayOfString = chat_send.split(",");
			for (String s : arrayOfString) {
				if (s.endsWith("*")) {
					s = s.substring(0, s.length() - 1);
					enabled.add(s);
				}
				all.add(s);
			}
			chatSendAll.put(p, all);
			chatSendEnabled.put(p, enabled);
		} else {
			chatSendAll.remove(p);
			chatSendEnabled.remove(p);
		}
		if (chat_receive != null) {
			Set<String> all = new HashSet<>();
			Set<String> enabled = new HashSet<>();
			arrayOfString = chat_receive.split(",");
			for (String a : arrayOfString) {
				if (a.endsWith("*")) {
					a = a.substring(0, a.length() - 1);
					enabled.add(a);
				}
				all.add(a);
			}
			chatReceiveAll.put(p, all);
			chatReceiveEnabled.put(p, enabled);
		} else {
			chatReceiveAll.remove(p);
			chatReceiveEnabled.remove(p);
		}
		if (prefix.size() > 0) {
			prefixes.put(p, prefix);
		} else {
			prefixes.remove(p);
		}
		if (suffix.size() > 0) {
			suffixes.put(p, suffix);
		} else {
			suffixes.remove(p);
		}
		if (messageformat != null) {
			messageFormat.put(p, messageformat);
		} else {
			messageFormat.remove(p);
		}
	}

	void setPermissions(Player p) {
		String playerName = p.getName();
		String w = p.getWorld().getName();

		PermissionAttachment pa = permissions.get(p);
		if (pa != null) {
			pa.remove();
		}
		pa = p.addAttachment(this);
		permissions.put(p, pa);
		permissionsRegistered = getServer().getPluginManager().getPermissions();

		List<String> grouplist = permissionsfile.getStringList("users." + playerName + ".group");
		if (grouplist.isEmpty()) {
			grouplist.addAll(defaultGroup);
		}
		if (debug) {
			getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "Debug for: " + playerName);
		}
		calculateGroupInheritance(pa, grouplist, w);

		applyPermissions(pa, permissionsfile.getStringList("users." + playerName + ".permissions"));
		calculateWorldUserInheritance(pa,
				permissionsfile.getStringList("users." + playerName + ".worlds." + w + ".inheritance"), playerName);
		applyPermissions(pa, permissionsfile.getStringList("users." + playerName + ".worlds." + w + ".permissions"));
	}

	private void applyPermissions(PermissionAttachment pa, List<String> perms) {
		int size = perms.size() - 1;
		for (int i = size; i >= 0; i--) {
			String perm = perms.get(i);
			boolean value = true;
			if (perm.startsWith("-")) {
				perm = perm.substring(1);
				value = false;
			}
			if ((perm.contains("*")) && (wildcard)) {
				String begin = perm.substring(0, perm.indexOf('*'));
				for (Permission per : permissionsRegistered) {
					String per_name = per.getName();
					if (per_name.startsWith(begin)) {
						if ((debug) && (extendedDebug)) {
							System.out.println(per_name + ": " + value);
						}
						pa.setPermission(per_name, value);
					}
				}
				if ((debug) && (!extendedDebug)) {
					if (value) {
						System.out.println(perm + ": true");
					} else {
						System.out.println(perm + ": false");
					}
				}
			} else {
				if (debug) {
					System.out.println(perm + ": " + value);
				}
				pa.setPermission(perm, value);
			}
		}
	}

	void checkUuid(Player p) {
		String uuid = p.getUniqueId().toString();
		String name = p.getName();
		String oldName = uuidfile.getString(uuid);
		if (oldName == null) {
			if (permissionsfile.get("users." + name) != null) {
				uuidfile.set(uuid, name);
				saveUuidFile();
			}
			return;
		}
		if (oldName.equals(name)) {
			return;
		}
		uuidfile.set(uuid, name);
		if (permissionsfile.get("users." + oldName) != null) {
			permissionsfile.set("users." + name, permissionsfile.get("users." + oldName));
			permissionsfile.set("users." + oldName, null);
			savePermissionFile();
		}
		saveUuidFile();
	}

	private void setGroup(String p_name, CommandSender s, String group_news) {
		if (permissionsfile.get("groups." + group_news) == null) {
			s.sendMessage(ChatColor.RED + "Group not found!");
			return;
		}
		Player p = getServer().getPlayer(p_name);
		if (p != null) {
			p_name = p.getName();
		}
		if ((!s.hasPermission("pm.setgroup")) && (!(s instanceof ConsoleCommandSender))) {
			boolean permission = false;

			List<String> grouplist = permissionsfile.getStringList("users." + p_name + ".group");
			if (grouplist.isEmpty()) {
				grouplist.addAll(defaultGroup);
			}
			if (grouplist.isEmpty()) {
				s.sendMessage(ChatColor.RED + "The player is not a member of a group!");
				return;
			}
			if (grouplist.size() > 1) {
				s.sendMessage(ChatColor.RED + "Cannot set group when the player is a member of multiple groups!");
				return;
			}
			if (permissionsfile.get("groups." + grouplist.get(0) + ".options.rank") == null) {
				s.sendMessage(ChatColor.RED + "Player has no current rank!");
				return;
			}
			if (permissionsfile.get("groups." + group_news + ".options.rank") == null) {
				s.sendMessage(ChatColor.RED + "The new group has no rank!");
				return;
			}
			Integer rank_old = Integer.valueOf(permissionsfile.getInt("groups." + grouplist.get(0) + ".options.rank"));
			Integer rank_new = Integer.valueOf(permissionsfile.getInt("groups." + group_news + ".options.rank"));
			String ladder = permissionsfile.getString("groups." + group_news + ".options.rank-ladder", "Default");
			if ((!s.hasPermission("pm.demote.all")) && (!s.hasPermission("pm.demote.own"))
					&& (rank_old.intValue() < rank_new.intValue())) {
				s.sendMessage(ChatColor.RED + "You have no permissions to demote " + p_name + "!");
				return;
			}
			if ((!s.hasPermission("pm.promote.all")) && (!s.hasPermission("pm.promote.own"))
					&& (rank_old.intValue() > rank_new.intValue())) {
				s.sendMessage(ChatColor.RED + "You have no permissions to promote " + p_name + "!");
				return;
			}
			List<String> grouplist_sender = permissionsfile.getStringList("users." + s.getName() + ".group");
			if (grouplist_sender.isEmpty()) {
				grouplist_sender.addAll(defaultGroup);
			}
			for (Iterator<String> localIterator = grouplist_sender.iterator(); localIterator.hasNext();) {
				String group = localIterator.next();
				if ((permissionsfile.get("groups." + group + ".options.rank") == null)
						|| ((!permissionsfile.getString("groups." + group + ".options.rank-ladder", "Default")
								.equals(ladder)) && (!s.hasPermission("pm.promote.all"))
								&& (!s.hasPermission("pm.demote.all")))
						|| (permissionsfile.getInt("groups." + group + ".options.rank") >= rank_new.intValue())
						|| (permissionsfile.getInt("groups." + group + ".options.rank") >= rank_old.intValue())) {
					break;
				}
				permission = true;
			}
			if (!permission) {
				s.sendMessage(ChatColor.RED + "You have no permissions to change " + p_name + "'s group!");
				return;
			}
		}
		List<String> group_new = new ArrayList<>();
		group_new.add(group_news);
		permissionsfile.set("users." + p_name + ".group", group_new);
		savePermissionFile();
		if (p != null) {
			setPrefixes(p);
			setPermissions(p);
		}
		s.sendMessage(ChatColor.GREEN + p_name + "'s group was succesfully changed to " + ChatColor.YELLOW
				+ group_new.get(0));
	}

	private void PromotePlayer(String p_name, CommandSender s) {
		Player p = getServer().getPlayer(p_name);
		if (p != null) {
			p_name = p.getName();
		}
		List<String> grouplist = permissionsfile.getStringList("users." + p_name + ".group");
		if (grouplist.isEmpty()) {
			grouplist.addAll(defaultGroup);
		}
		if (grouplist.isEmpty()) {
			s.sendMessage(ChatColor.RED + "Player is not a member of a group!");
			return;
		}
		if (grouplist.size() > 1) {
			s.sendMessage(ChatColor.RED + "Members of multiple groups are not promotable!");
			return;
		}
		if (permissionsfile.get("groups." + grouplist.get(0) + ".options.rank") == null) {
			s.sendMessage(ChatColor.RED + "Player has no current rank!");
			return;
		}
		String ladder = permissionsfile.getString("groups." + grouplist.get(0) + ".options.rank-ladder", "Default");
		int rank_old = permissionsfile.getInt("groups." + grouplist.get(0) + ".options.rank");

		int index = rankMapRank.get(ladder).indexOf(Integer.valueOf(rank_old)) - 1;
		if (index == -1) {
			s.sendMessage(ChatColor.RED + "No higher rank for this player has been found!");
			return;
		}
		Integer rank_new = rankMapRank.get(ladder).get(index);
		String group_news = rankMapGroup.get(ladder).get(rank_new);
		if ((!s.hasPermission("pm.promote.all")) && (!(s instanceof ConsoleCommandSender))) {
			List<String> grouplist_sender = permissionsfile.getStringList("users." + s.getName() + ".group");
			if (grouplist_sender.isEmpty()) {
				grouplist_sender.addAll(defaultGroup);
			}
			boolean permission = false;
			for (Iterator<String> localIterator = grouplist_sender.iterator(); localIterator.hasNext();) {
				String group = localIterator.next();
				if ((permissionsfile.get("groups." + group + ".options.rank") == null)
						|| (!permissionsfile.getString("groups." + group + ".options.rank-ladder", "Default")
								.equals(ladder))
						|| (permissionsfile.getInt("groups." + group + ".options.rank") >= rank_new.intValue())) {
					break;
				}
				permission = true;
			}
			if (!permission) {
				s.sendMessage(ChatColor.RED + "You have no permissions to promote " + p_name + "!");
				return;
			}
		}
		List<String> group_new = new ArrayList<>();
		group_new.add(group_news);
		permissionsfile.set("users." + p_name + ".group", group_new);
		savePermissionFile();
		if (p != null) {
			setPrefixes(p);
			setPermissions(p);
		}
		s.sendMessage(ChatColor.GREEN + p_name + " was succesfully promoted to " + ChatColor.YELLOW + group_new.get(0));
	}

	private void DemotePlayer(String p_name, CommandSender s) {
		Player p = Bukkit.getPlayer(p_name);
		if (p != null) {
			p_name = p.getName();
		}
		List<String> grouplist = permissionsfile.getStringList("users." + p_name + ".group");
		if (grouplist.isEmpty()) {
			grouplist.addAll(defaultGroup);
		}
		if (grouplist.isEmpty()) {
			s.sendMessage(ChatColor.RED + "Player is not a member of a group!");
			return;
		}
		if (grouplist.size() > 1) {
			s.sendMessage(ChatColor.RED + "Members of multiple groups are not demotable!");
			return;
		}
		if (permissionsfile.get("groups." + grouplist.get(0) + ".options.rank") == null) {
			s.sendMessage(ChatColor.RED + "Player has no current rank!");
			return;
		}
		String ladder = permissionsfile.getString("groups." + grouplist.get(0) + ".options.rank-ladder", "Default");
		int rank_old = permissionsfile.getInt("groups." + grouplist.get(0) + ".options.rank");

		int index = rankMapRank.get(ladder).indexOf(Integer.valueOf(rank_old)) + 1;
		if (index == rankMapRank.get(ladder).size()) {
			s.sendMessage(ChatColor.RED + "No lower rank for this player has been found!");
			return;
		}
		Integer rank_new = rankMapRank.get(ladder).get(index);
		String group_news = rankMapGroup.get(ladder).get(rank_new);
		if ((!s.hasPermission("pm.demote.all")) && (!(s instanceof ConsoleCommandSender))) {
			List<String> grouplist_sender = permissionsfile.getStringList("users." + s.getName() + ".group");
			if (grouplist_sender.isEmpty()) {
				grouplist_sender.addAll(defaultGroup);
			}
			boolean permission = false;
			for (Iterator<String> localIterator = grouplist_sender.iterator(); localIterator.hasNext();) {
				String group = localIterator.next();
				if ((permissionsfile.get("groups." + group + ".options.rank") == null)
						|| (!permissionsfile.getString("groups." + group + ".options.rank-ladder", "Default")
								.equals(ladder))
						|| (permissionsfile.getInt("groups." + group + ".options.rank") >= rank_old)) {
					break;
				}
				permission = true;
			}
			if (!permission) {
				s.sendMessage(ChatColor.RED + "You have no permissions to demote " + p_name + "!");
				return;
			}
		}
		List<String> group_new = new ArrayList<>();
		group_new.add(group_news);
		permissionsfile.set("users." + p_name + ".group", group_new);
		savePermissionFile();
		if (p != null) {
			setPrefixes(p);
			setPermissions(p);
		}
		s.sendMessage(ChatColor.GREEN + p_name + " was succesfully demoted to " + ChatColor.YELLOW + group_new.get(0));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ((cmd.getName().equalsIgnoreCase("pm")) && (args.length == 1) && (args[0].equalsIgnoreCase("reload"))) {
			if ((!sender.hasPermission("pm.reload")) && (!(sender instanceof ConsoleCommandSender))) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to reload!");
				return true;
			}
			reload();
			sender.sendMessage(ChatColor.GREEN + "Permission Manager reloaded!");
			return true;
		}
		if ((cmd.getName().equalsIgnoreCase("pm")) && (args.length == 1) && (args[0].equalsIgnoreCase("debug"))) {
			if ((!sender.hasPermission("pm.debug")) && (!(sender instanceof ConsoleCommandSender))) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to toggle debug!");
				return true;
			}
			if (debug) {
				debug = false;
				sender.sendMessage(ChatColor.GREEN + "Debug disabled!");
			} else {
				debug = true;
				sender.sendMessage(ChatColor.GREEN + "Debug enabled!");
			}
			return true;
		}
		if ((cmd.getName().equalsIgnoreCase("pm")) && (args.length == 2) && (args[0].equalsIgnoreCase("promote"))) {
			if ((!sender.hasPermission("pm.promote.own")) && (!sender.hasPermission("pm.promote.all"))
					&& (!(sender instanceof ConsoleCommandSender))) {
				sender.sendMessage(ChatColor.RED + "You have no permissions to promote " + args[1] + "!");
				return true;
			}
			PromotePlayer(args[1], sender);
			return true;
		}
		if ((cmd.getName().equalsIgnoreCase("pm")) && (args.length == 3) && (args[0].equalsIgnoreCase("setgroup"))) {
			if ((!sender.hasPermission("pm.setgroup")) && (!sender.hasPermission("pm.promote.own"))
					&& (!sender.hasPermission("pm.promote.all")) && (!sender.hasPermission("pm.demote.own"))
					&& (!sender.hasPermission("pm.demote.all")) && (!(sender instanceof ConsoleCommandSender))) {
				sender.sendMessage(ChatColor.RED + "You have no permissions to change " + args[1] + "'s group!");
				return true;
			}
			setGroup(args[1], sender, args[2]);
			return true;
		}
		if ((cmd.getName().equalsIgnoreCase("pm")) && (args.length == 2) && (args[0].equalsIgnoreCase("demote"))) {
			if ((!sender.hasPermission("pm.demote.own")) && (!sender.hasPermission("pm.demote.all"))
					&& (!(sender instanceof ConsoleCommandSender))) {
				sender.sendMessage(ChatColor.RED + "You have no permissions to demote " + args[1] + "!");
				return true;
			}
			DemotePlayer(args[1], sender);
			return true;
		}
		if ((cmd.getName().equalsIgnoreCase("chat")) && (args.length == 1) && (args[0].equalsIgnoreCase("receive"))) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can be used by a player only!");
				return true;
			}
			if (!getConfig().getBoolean("Settings.Chat.Chat_Channels.Enabled")) {
				return true;
			}
			if (!sender.hasPermission("pm.chat.receive.view")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
				return true;
			}
			Set<String> chat_all = chatReceiveAll.get(sender);
			if (chat_all == null) {
				sender.sendMessage(ChatColor.RED + "No chat channels found!");
				return true;
			}
			StringBuffer b = new StringBuffer();
			for (String a : chat_all) {
				if (chatReceiveEnabled.get(sender).contains(a)) {
					b.append(" " + ChatColor.GREEN + a);
				} else {
					b.append(" " + ChatColor.RED + a);
				}
			}
			sender.sendMessage(ChatColor.AQUA + "Chat-Channels Receive:" + b.toString());
			return true;
		}
		if ((cmd.getName().equalsIgnoreCase("chat")) && (args.length == 3) && (args[0].equalsIgnoreCase("receive"))
				&& (args[1].equalsIgnoreCase("toggle"))) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can be used by a player only!");
				return true;
			}
			if (!getConfig().getBoolean("Settings.Chat.Chat_Channels.Enabled")) {
				return true;
			}
			if (!sender.hasPermission("pm.chat.receive.toggle")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
				return true;
			}
			if ((chatReceiveAll.get(sender) != null) && (chatReceiveAll.get(sender).contains(args[2]))) {
				if (chatReceiveEnabled.get(sender).contains(args[2])) {
					chatReceiveEnabled.get(sender).remove(args[2]);
					sender.sendMessage(ChatColor.AQUA + "Chat-Channel " + ChatColor.YELLOW + args[2] + ChatColor.RED
							+ " disabled " + ChatColor.AQUA + "for receiving.");
				} else {
					chatReceiveEnabled.get(sender).add(args[2]);
					sender.sendMessage(ChatColor.AQUA + "Chat-Channel " + ChatColor.YELLOW + args[2] + ChatColor.GREEN
							+ " enabled " + ChatColor.AQUA + "for receiving.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Chat-Channel not found: " + args[2]);
			}
			return true;
		}
		if ((cmd.getName().equalsIgnoreCase("chat")) && (args.length == 1) && (args[0].equalsIgnoreCase("send"))) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can be used by a player only!");
				return true;
			}
			if (!getConfig().getBoolean("Settings.Chat.Chat_Channels.Enabled")) {
				return true;
			}
			if (!sender.hasPermission("pm.chat.send.view")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
				return true;
			}
			Set<String> chat_all = chatSendAll.get(sender);
			if (chat_all == null) {
				sender.sendMessage(ChatColor.RED + "No chat channels found!");
				return true;
			}
			StringBuffer b = new StringBuffer();
			for (String a : chat_all) {
				if (chatSendEnabled.get(sender).contains(a)) {
					b.append(" " + ChatColor.GREEN + a);
				} else {
					b.append(" " + ChatColor.RED + a);
				}
			}
			sender.sendMessage(ChatColor.AQUA + "Chat-Channels Send:" + b.toString());
			return true;
		}
		if ((cmd.getName().equalsIgnoreCase("chat")) && (args.length == 3) && (args[0].equalsIgnoreCase("send"))
				&& (args[1].equalsIgnoreCase("toggle"))) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can be used by a player only!");
				return true;
			}
			if (!getConfig().getBoolean("Settings.Chat.Chat_Channels.Enabled")) {
				return true;
			}
			if (!sender.hasPermission("pm.chat.send.toggle")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
				return true;
			}
			if ((chatSendAll.get(sender) != null) && (chatSendAll.get(sender).contains(args[2]))) {
				if (chatSendEnabled.get(sender).contains(args[2])) {
					chatSendEnabled.get(sender).remove(args[2]);
					sender.sendMessage(ChatColor.AQUA + "Chat-Channel " + ChatColor.YELLOW + args[2] + ChatColor.RED
							+ " disabled " + ChatColor.AQUA + "for sending.");
				} else {
					chatSendEnabled.get(sender).add(args[2]);
					sender.sendMessage(ChatColor.AQUA + "Chat-Channel " + ChatColor.YELLOW + args[2] + ChatColor.GREEN
							+ " enabled " + ChatColor.AQUA + "for sending.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Chat-Channel not found: " + args[2]);
			}
			return true;
		}
		return false;
	}
}
