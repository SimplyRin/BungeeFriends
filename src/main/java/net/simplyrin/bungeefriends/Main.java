package net.simplyrin.bungeefriends;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bstats.bungeecord.Metrics;

import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.simplyrin.bungeefriends.commands.ChatCommand;
import net.simplyrin.bungeefriends.commands.FriendCommand;
import net.simplyrin.bungeefriends.commands.FriendConsoleCommand;
import net.simplyrin.bungeefriends.commands.PartyChatCommand;
import net.simplyrin.bungeefriends.commands.PartyCommand;
import net.simplyrin.bungeefriends.commands.ReplyCommand;
import net.simplyrin.bungeefriends.commands.TellCommand;
import net.simplyrin.bungeefriends.commands.alias.FLCommand;
import net.simplyrin.bungeefriends.listeners.EventListener;
import net.simplyrin.bungeefriends.listeners.parties.PartyListener;
import net.simplyrin.bungeefriends.tools.Priority;
import net.simplyrin.bungeefriends.tools.ThreadPool;
import net.simplyrin.bungeefriends.utils.ConfigManager;
import net.simplyrin.bungeefriends.utils.DataManager;
import net.simplyrin.bungeefriends.utils.FriendManager;
import net.simplyrin.bungeefriends.utils.LanguageManager;
import net.simplyrin.bungeefriends.utils.MessageBuilder;
import net.simplyrin.bungeefriends.utils.MySQLManager;
import net.simplyrin.bungeefriends.utils.PartyManager;
import net.simplyrin.bungeefriends.utils.PlayerManager;
import net.simplyrin.bungeefriends.utils.PrefixManager;

/**
 * Created by SimplyRin on 2018/07/03.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
@Getter
public class Main extends Plugin {

	private ConfigManager configManager;
	private DataManager dataManager;
	private PrefixManager prefixManager;
	private PlayerManager playerManager;
	private FriendManager friendManager;
	private PartyManager partyManager;
	private LanguageManager languageManager;
	private MySQLManager mySQLManager;

	private Priority priority;
	private Map<UUID, UUID> replyTargetMap;

	private boolean isEnabledMySQL;

	private FriendCommand friendCommand;
	private FriendCommand friendFCommand;
	private FriendConsoleCommand friendConsoleCommand;
	private ReplyCommand replyCommand;
	private ReplyCommand replyRCommand;
	private FLCommand flCommand;
	private PartyCommand partyCommand;
	private ChatCommand chatCommand;
	private PartyChatCommand partyChatCommand;

	private TellCommand tellCommand;
	private TellCommand tellMsgCommand;
	private TellCommand tellMessageCommand;
	private TellCommand tellWCommand;
	private TellCommand tellWhisperCommand;

	private EventListener eventListener;
	private PartyListener partyListener;

	private LuckPerms luckPermsApi;

	private UUID consoleUniqueId = UUID.fromString("f2304371-010c-41fd-9548-40b34325b263");

	@Override
	public void onEnable() {
		this.configManager = new ConfigManager(this);
		this.dataManager = new DataManager(this);
		this.prefixManager = new PrefixManager(this);
		this.playerManager = new PlayerManager(this);
		this.friendManager = new FriendManager(this);

		this.mySQLManager = new MySQLManager(this);

		this.priority = new Priority(this);
		this.priority.update();

		this.reloadLanguageManager();
		this.reloadPartyCmdSender();

		this.friendCommand = new FriendCommand(this, "friend");
		this.getProxy().getPluginManager().registerCommand(this, this.friendCommand);
		this.friendConsoleCommand = new FriendConsoleCommand(this, "friendconsole");
		this.getProxy().getPluginManager().registerCommand(this, this.friendConsoleCommand);

		this.reloadCommandAliases();

		if (this.configManager.getConfig().getBoolean("Plugin.Enable-BungeeParties")) {
			this.partyManager = new PartyManager(this);

			this.getProxy().getPluginManager().registerCommand(this, new PartyChatCommand(this, "pchat"));
			this.getProxy().getPluginManager().registerCommand(this, new PartyChatCommand(this, "partychat"));

			this.partyCommand = new PartyCommand(this);
			this.getProxy().getPluginManager().registerCommand(this, this.partyCommand);

			this.partyListener = new PartyListener(this);
			this.getProxy().getPluginManager().registerListener(this, this.partyListener);

			this.chatCommand = new ChatCommand(this, "chat");
			this.getProxy().getPluginManager().registerCommand(this, this.chatCommand);
		}

		this.eventListener = new EventListener(this);
		this.getProxy().getPluginManager().registerListener(this, this.eventListener);

		this.replyTargetMap = new HashMap<>();
		this.isEnabledMySQL = this.mySQLManager.getConfig().getBoolean("Enable");

		if (this.getProxy().getPluginManager().getPlugin("LuckPerms") != null
				&& !this.configManager.getConfig().getBoolean("Plugin.Disable-LuckPerms-API")) {
			try {
				this.luckPermsApi = LuckPermsProvider.get();
				this.info("&aLoaded LuckPerms API.");
			} catch (Exception e) {
				this.info("&cFailed loading LuckPerms API.");
			}
		}

		this.info("&b&m--------------------------------");
		this.info("&bBungeeFriends has been loaded.");
		String prefix = "";
		if (this.getDescription().getVersion().toLowerCase().contains("beta")) {
			prefix += "Beta ";
		}
		this.info("&b&m--------------------------------");

		BungeeFriends.getAPI().setInstance(this);

		new Metrics(this);
	}

	@Override
	public void onDisable() {
		this.dataManager.save();
		this.playerManager.save();

		if (this.isEnabledMySQL) {
			this.mySQLManager.getEditor().getMySQL().disconnect();
		}
	}

	public void reloadCommandAliases() {
		if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./msg")) {
			this.tellMsgCommand = new TellCommand(this, "msg");
			this.getProxy().getPluginManager().registerCommand(this, this.tellMsgCommand);
		} else if(this.tellMsgCommand != null) {
			this.getProxy().getPluginManager().unregisterCommand(this.tellMsgCommand);
		}

		if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./tell")) {
			this.tellCommand = new TellCommand(this, "tell");
			this.getProxy().getPluginManager().registerCommand(this, this.tellCommand);
		} else if(this.tellCommand != null) {
			this.getProxy().getPluginManager().unregisterCommand(this.tellCommand);
		}

		if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./w")) {
			this.tellWCommand = new TellCommand(this, "w");
			this.getProxy().getPluginManager().registerCommand(this, this.tellWCommand);
		} else if(this.tellWCommand != null) {
			this.getProxy().getPluginManager().unregisterCommand(this.tellWCommand);
		}

		if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./whisper")) {
			this.tellWhisperCommand = new TellCommand(this, "whisper");
			this.getProxy().getPluginManager().registerCommand(this, this.tellWhisperCommand);
		} else if(this.tellWhisperCommand != null) {
			this.getProxy().getPluginManager().unregisterCommand(this.tellWhisperCommand);
		}

		if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./message")) {
			this.tellMessageCommand = new TellCommand(this, "message");
			this.getProxy().getPluginManager().registerCommand(this, this.tellMessageCommand);
		} else if(this.tellMessageCommand != null) {
			this.getProxy().getPluginManager().unregisterCommand(this.tellMessageCommand);
		}

		if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./f")) {
			this.friendFCommand = new FriendCommand(this, "f");
			this.getProxy().getPluginManager().registerCommand(this, this.friendFCommand);
		} else if(this.friendFCommand != null) {
			this.getProxy().getPluginManager().unregisterCommand(this.friendFCommand);
		}

		if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./r")) {
			this.replyRCommand = new ReplyCommand(this, "r");
			this.getProxy().getPluginManager().registerCommand(this, this.replyRCommand);
		} else if(this.replyRCommand != null) {
			this.getProxy().getPluginManager().unregisterCommand(this.replyRCommand);
		}

		if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./reply")) {
			this.replyCommand = new ReplyCommand(this, "reply");
			this.getProxy().getPluginManager().registerCommand(this, this.replyCommand);
		} else if(this.replyCommand != null) {
			this.getProxy().getPluginManager().unregisterCommand(this.replyCommand);
		}

		if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./fl")) {
			this.flCommand = new FLCommand(this);
			this.getProxy().getPluginManager().registerCommand(this, this.flCommand);
		} else if(this.flCommand != null) {
			this.getProxy().getPluginManager().unregisterCommand(this.flCommand);
		}

		if (this.configManager.getConfig().getBoolean("Plugin.Enable-BungeeParties")) {
			if (!this.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./pc")) {
				this.partyChatCommand = new PartyChatCommand(this, "pc");
				this.getProxy().getPluginManager().registerCommand(this, this.partyChatCommand);
			} else if(this.partyChatCommand != null) {
				this.getProxy().getPluginManager().unregisterCommand(this.partyChatCommand);
			}
		}
	}

	public void reloadLanguageManager() {
		this.languageManager = new LanguageManager(this);
	}

	private HashMap<String, String> partyCmdSenderMap = new HashMap<>();

	public void reloadPartyCmdSender() {
		this.partyCmdSenderMap.clear();

		String base = "Plugin.BungeeParties.PartyCmdSender";
		for (String key : this.getConfigManager().getConfig().getSection("Plugin.BungeeParties.PartyCmdSender").getKeys()) {
			String leader = this.getString(base + "." + key + ".Leader");
			String member = this.getString(base + "." + key + ".Member");

			if (!leader.equals("") && !member.equals("")) {
				this.partyCmdSenderMap.put(leader, member);
			}
		}
	}

	public String getServerName(String serverName) {
		for (String key : this.getConfigManager().getConfig().getSection("Plugin.ServerName").getKeys()) {
			String type = this.getString("Plugin.ServerName." + key + ".Type");
			String name = this.getString("Plugin.ServerName." + key + ".ServerName");
			String replaceName = this.getString("Plugin.ServerName." + key + ".ReplaceName");

			if (!type.equals("") && !name.equals("") && !replaceName.equals("")) {
				if (type.equals("startsWith") && serverName.startsWith(name)) {
					serverName = replaceName;
				} else if (type.equals("contains") && serverName.contains(name)) {
					serverName = replaceName;
				} else if ((type.equals("equals") || type.equals("equalsIgnoreCase")) && serverName.equalsIgnoreCase(name)) {
					serverName = replaceName;
				}
			}
		}
		return serverName;
	}

	public String getPrefix() {
		return this.configManager.getConfig().getString("Plugin.Prefix");
	}

	public String getString(String key) {
		if (this.isEnabledMySQL && key.startsWith("Player.") && !key.endsWith(".Requests")) {
			if (this.getMySQLManager().isDebugMode()) {
				System.out.println("Checking key: " + key);
			}
			return this.getMySQLManager().getEditor().get(key);
		}
		if (key.startsWith("Player.") || key.startsWith("Parties.")) {
			return this.getDataManager().getConfig().getString(key);
		}
		if (key.startsWith("Name.") || key.startsWith("UUID.")) {
			return this.getPlayerManager().getConfig().getString(key);
		}
		return this.getConfigManager().getConfig().getString(key);
	}

	public List<String> getStringList(String key) {
		if (this.isEnabledMySQL && key.startsWith("Player.") && !key.endsWith(".Requests")) {
			if (this.getMySQLManager().isDebugMode()) {
				System.out.println("Checking key: " + key);
			}
			return this.getMySQLManager().getEditor().getList(key);
		}
		if (key.startsWith("Player.") || key.startsWith("Parties.")) {
			return this.getDataManager().getConfig().getStringList(key);
		}
		if (key.startsWith("Name.") || key.startsWith("UUID.")) {
			return this.getPlayerManager().getConfig().getStringList(key);
		}
		return this.getConfigManager().getConfig().getStringList(key);
	}

	public boolean getBoolean(String key) {
		if (this.isEnabledMySQL && key.startsWith("Player.")) {
			if (this.getMySQLManager().isDebugMode()) {
				System.out.println("Checking key: " + key);
			}
			return Boolean.valueOf(this.getMySQLManager().getEditor().get(key));
			// return Boolean.valueOf(this.sqlCache(key));
		}
		if (key.startsWith("Player.") || key.startsWith("Parties.")) {
			return Boolean.valueOf(this.getDataManager().getConfig().getBoolean(key));
		}
		if (key.startsWith("Name.") || key.startsWith("UUID.")) {
			return Boolean.valueOf(this.getPlayerManager().getConfig().getBoolean(key));
		}
		return Boolean.valueOf(this.getConfigManager().getConfig().getBoolean(key));
	}

	public void set(String key, List<String> list) {
		if (this.isEnabledMySQL && key.startsWith("Player.") && !key.endsWith(".Requests")) {
			if (this.getMySQLManager().isDebugMode()) {
				System.out.println("Setting key: " + key);
			}
			ThreadPool.run(() -> {
				this.getMySQLManager().getEditor().set(key, list);
			});
		} else if (key.startsWith("Player.") || key.startsWith("Parties.")) {
			this.getDataManager().getConfig().set(key, list);
		} else if (key.startsWith("Name.") || key.startsWith("UUID.")) {
			this.getPlayerManager().getConfig().set(key, list);
		} else {
			this.getConfigManager().getConfig().set(key, list);
		}
	}

	public void set(String key, String value) {
		if (this.isEnabledMySQL && key.startsWith("Player.") && !key.endsWith(".Requests")) {
			if (this.getMySQLManager().isDebugMode()) {
				System.out.println("Setting key: " + key);
			}
			ThreadPool.run(() -> {
				this.getMySQLManager().getEditor().set(key, String.valueOf(value));
			});
		}
		else if (key.startsWith("Player.") || key.startsWith("Parties.")) {
			this.getDataManager().getConfig().set(key, value);
		}
		else if (key.startsWith("Name.") || key.startsWith("UUID.")) {
			this.getPlayerManager().getConfig().set(key, value);
		}
		else {
			this.getConfigManager().getConfig().set(key, value);
		}
	}

	public void set(String key, boolean value) {
		if (this.isEnabledMySQL && key.startsWith("Player.")) {
			if (this.getMySQLManager().isDebugMode()) {
				System.out.println("Setting key: " + key);
			}
			ThreadPool.run(() -> {
				this.getMySQLManager().getEditor().set(key, String.valueOf(value));
			});
		} else if (key.startsWith("Player.") || key.startsWith("Parties.")) {
			this.getDataManager().getConfig().set(key, value);
		} else if (key.startsWith("Name.") || key.startsWith("UUID.")) {
			this.getPlayerManager().getConfig().set(key, value);
		} else {
			this.getConfigManager().getConfig().set(key, value);
		}
	}

	@SuppressWarnings("deprecation")
	public void info(String args) {
		this.getProxy().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&cBungeeFriends&7] &r" + args));
	}

	public void info(String player, String args) {
		if (args.equals("") || args == null) {
			return;
		}
		this.info(this.getProxy().getPlayer(player), args);
	}

	@SuppressWarnings("deprecation")
	public void info(ProxiedPlayer player, String args) {
		if (player == null || args.equals("") || args == null) {
			return;
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
	}

	@SuppressWarnings("deprecation")
	public void info(UUID uuid, String args) {
		if (args.equals("") || args == null) {
			return;
		}
		ProxiedPlayer player = this.getProxy().getPlayer(uuid);
		if (player != null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
		}
	}

	public void info(ProxiedPlayer player, TextComponent args) {
		if (args.getText().equals("") || args == null) {
			return;
		}
		player.sendMessage(MessageBuilder.get(this.getPrefix()), args);
	}

}
