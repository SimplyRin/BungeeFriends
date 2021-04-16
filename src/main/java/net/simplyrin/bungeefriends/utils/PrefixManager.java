package net.simplyrin.bungeefriends.utils;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.tools.Config;

/**
 * Created by SimplyRin on 2018/09/04.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class PrefixManager {

	private Main plugin;
	@Getter
	private Runnable runnable;
	@Getter
	private Configuration config = null;

	public PrefixManager(Main plugin) {
		this.plugin = plugin;

		if (this.plugin.getBoolean("Plugin.Enable-Deprecated-PrefixYml")) {
			this.createConfig();
			this.reload();
		} else {
			this.backup();
		}
	}

	public void backup() {
		File file = new File(this.plugin.getDataFolder(), "prefix.yml");
		if (file.exists()) {
			File to = new File(this.plugin.getDataFolder(), "prefix.yml_bak");
			file.renameTo(to);

			this.plugin.info("&cprefix.yml has been removed in BungeeFriends v2.1.3+. Now use the LuckPerms API.");
			this.plugin.info("&cIf you disable the LuckPerms API, check your config.yml.");
		}
	}

	public void reload() {
		File folder = this.plugin.getDataFolder();
		File file = new File(folder, "prefix.yml");
		if (file.exists()) {
			this.config = Config.getConfig(new File(folder, "prefix.yml"), Charsets.UTF_8);
		}
	}

	public void createConfig() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		File prefix = new File(folder, "prefix.yml");
		if (!prefix.exists()) {
			try {
				prefix.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(prefix, Charsets.UTF_8);

			this.config.set("List.Owner.Prefix", "&c[OWNER] ");
			this.config.set("List.Owner.Permission", "friends.prefix.owner");

			this.config.set("List.Admin.Prefix", "&c[ADMIN] ");
			this.config.set("List.Admin.Permission", "friends.prefix.admin");

			this.config.set("List.MVP+.Prefix", "&b[MVP&c+&b] ");
			this.config.set("List.MVP+.Permission", "friends.prefix.mvp_plus");

			this.config.set("List.MVP.Prefix", "&b[MVP] ");
			this.config.set("List.MVP.Permission", "friends.prefix.mvp");

			this.config.set("List.VIP+.Prefix", "&a[VIP&6+&a] ");
			this.config.set("List.VIP+.Permission", "friends.prefix.vip_plus");

			this.config.set("List.VIP.Prefix", "&a[VIP] ");
			this.config.set("List.VIP.Permission", "friends.prefix.vip");

			Config.saveConfig(this.config, prefix);
		} else {
			this.reload();
		}


	}

}
