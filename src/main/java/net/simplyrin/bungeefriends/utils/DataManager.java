package net.simplyrin.bungeefriends.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.google.common.base.Charsets;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.tools.Config;

/**
 * Created by SimplyRin on 2021/03/09.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class DataManager {

	private Main plugin;
	@Getter
	private Runnable runnable;
	@Getter
	private Configuration config;

	public DataManager(Main plugin) {
		this.plugin = plugin;

		this.autoMigration();
		this.save();
	}

	public void autoMigration() {
		ConfigManager manager = this.plugin.getConfigManager();

		File configF = new File(this.plugin.getDataFolder(), "data.yml");
		if (!configF.exists()) {
			try {
				configF.createNewFile();
			} catch (IOException e) {
			}
			Config.saveConfig(manager.getConfig(), configF);
			this.config = Config.getConfig(configF, Charsets.UTF_8);
			this.config.set("Plugin", null);
			this.config.set("BungeeParties", null);
			this.config.set("UUID", null);
			this.config.set("Name", null);

			this.save();

			manager.getConfig().set("Player", null);
			manager.getConfig().set("Parties", null);
			manager.save();
		}

		this.config = Config.getConfig(configF, Charsets.UTF_8);

		for (String value : this.config.getSection("Parties").getKeys()) {
			this.config.set("Parties." + value + ".Currently-Joined-Party", "NONE");
			this.config.set("Parties." + value + ".Party-List", new ArrayList<>());
		}
		this.save();
	}

	public void save() {
		File config = new File(this.plugin.getDataFolder(), "data.yml");
		Config.saveConfig(this.config, config);
	}

}
