package net.simplyrin.bungeefriends.utils;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.tools.Config;

/**
 * Created by SimplyRin on 2018/07/03.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class ConfigManager {

	private Main plugin;
	@Getter
	private Runnable runnable;
	@Getter
	private Configuration config;

	public ConfigManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		this.save();
	}

	public void save() {
		File config = new File(this.plugin.getDataFolder(), "config.yml");
		Config.saveConfig(this.config, config);
	}

	public void reload() {
		File config = new File(this.plugin.getDataFolder(), "config.yml");
		this.config = Config.getConfig(config, Charsets.UTF_8);

		this.plugin.reloadCommandAliases();
		this.plugin.reloadPartyCmdSender();
		this.plugin.getPriority().update();
	}

	public void createConfig() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		File config = new File(folder, "config.yml");
		if (!config.exists()) {
			try {
				config.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(config, Charsets.UTF_8);

			this.config.set("Plugin.Prefix", "&7[&cFriends&7] &r");

			this.config.set("Plugin.Disable-Aliases./msg", false);
			this.config.set("Plugin.Disable-Aliases./f", false);
			this.config.set("Plugin.Disable-Aliases./r", false);
			this.config.set("Plugin.Disable-Aliases./reply", false);
			this.config.set("Plugin.Disable-Aliases./fl", false);
			this.config.set("Plugin.Default-Language", "english");

			/** this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Name", "SimplyRin");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Language", "english");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Prefix", "&c[CREATOR] ");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Toggle", true);
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Friends", (List<String>) Arrays.asList("64636120-8633-4541-aa5f-412b42ddb04d"));

			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Name", "SimplyFoxy");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Language", "english");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Prefix", "&c[CREATOR] ");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Toggle", true);
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Friends", (List<String>) Arrays.asList("b0bb65a2-832f-4a5d-854e-873b7c4522ed")); */

			Config.saveConfig(this.config, config);
		}

		this.config = Config.getConfig(config, Charsets.UTF_8);

		this.config.set("Plugin.Disable-Alias", null);

		this.resetValue("Plugin.Disable-Aliases./msg");
		this.resetValue("Plugin.Disable-Aliases./tell");
		this.resetValue("Plugin.Disable-Aliases./message");
		this.resetValue("Plugin.Disable-Aliases./f");
		this.resetValue("Plugin.Disable-Aliases./r");
		this.resetValue("Plugin.Disable-Aliases./reply");
		this.resetValue("Plugin.Disable-Aliases./fl");
		this.resetValue("Plugin.Disable-Aliases./pc");
		this.resetValue("Plugin.Disable-Aliases./w");
		this.resetValue("Plugin.Disable-Aliases./whisper");
		this.resetValue("Plugin.Disable-Lang-Command");
		this.resetValue("Plugin.Disable-ServerSwitchNotifer");
		this.resetValue("Plugin.Disable-PartyWarp");
		this.resetValue("Plugin.Disable-LuckPerms-API");
		this.resetValue("Plugin.Disable-Hyphen");
		this.resetValue("Plugin.Disable-TabComplete");
		this.resetValue("Plugin.Disable-PartyCmdSender");
		this.resetValue("Plugin.Enable-BungeeParties");
		this.resetValue("Plugin.Enable-Deprecated-PrefixYml");
		this.resetValue("Plugin.FriendList-NoPrefix");

		this.resetStringValue("Plugin.Default-Language", "english");
		this.resetStringValue("Plugin.Bypass-Lobby-Name-Contains", "lobby");

		this.resetStringValue("Plugin.ServerName.1.Type", "startsWith");
		this.resetStringValue("Plugin.ServerName.1.ServerName", "lobby-");
		this.resetStringValue("Plugin.ServerName.1.ReplaceName", "Lobby");

		this.resetStringValue("Plugin.ServerName.2.Type", "contains");
		this.resetStringValue("Plugin.ServerName.2.ServerName", "game");
		this.resetStringValue("Plugin.ServerName.2.ReplaceName", "Game");

		this.resetStringValue("Plugin.ServerName.3.Type", "equalsIgnoreCase");
		this.resetStringValue("Plugin.ServerName.3.ServerName", "sw-1");
		this.resetStringValue("Plugin.ServerName.3.ReplaceName", "SkyWars");

		this.resetStringValue("Plugin.BungeeParties.PartyCmdSender.1.Leader", "/join");
		this.resetStringValue("Plugin.BungeeParties.PartyCmdSender.1.Member", "/join $args");

		this.resetStringValue("Plugin.BungeeParties.PartyCmdSender.2.Leader", "/sw join");
		this.resetStringValue("Plugin.BungeeParties.PartyCmdSender.2.Member", "/sw join $args");

		this.resetStringValue("Plugin.FriendList-Sort.1.NoColorPrefix", "[ADMIN]");
		this.resetStringValue("Plugin.FriendList-Sort.1.Priority", 1);

		this.resetStringValue("Plugin.FriendList-Sort.2.NoColorPrefix", "[MOD]");
		this.resetStringValue("Plugin.FriendList-Sort.2.Priority", 2);

		this.config.set("Plugin.Disable-Party-ServerSwitchNotifer", null);
		this.config.set("Plugin.Disable-Lang-Command", null);
		this.config.set("Plugin.Use-LuckPerms-API", null);

		if (this.config.getBoolean("BungeeParties.Enable")) {
			this.config.set("Plugin.Enable-BungeeParties", true);
			this.config.set("BungeeParties.Enable", null);
		} else {
			this.config.set("BungeeParties.Enable", null);
		}

		this.save();
	}

	public void resetStringValue(String key, String value) {
		if (this.config.getString(key).equals("")) {
			this.config.set(key, value);
		}
	}

	public void resetStringValue(String key, int value) {
		if (this.config.getInt(key, -1) == -1) {
			this.config.set(key, value);
		}
	}

	public void resetValue(String key) {
		if (!this.config.getBoolean(key)) {
			this.config.set(key, false);
		}
	}

}
