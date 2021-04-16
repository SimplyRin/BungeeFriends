package net.simplyrin.bungeefriends.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import com.google.common.base.Charsets;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Listener;
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
public class PlayerManager implements Listener {

	private Main plugin;
	@Getter
	private Runnable runnable;
	@Getter
	private Configuration config;

	public PlayerManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();

		boolean hasKey = false;
		for (String key : this.plugin.getConfigManager().getConfig().getSection("UUID").getKeys()) {
			hasKey = true;
			this.config.set("UUID." + key, this.plugin.getConfigManager().getConfig().get("UUID." + key));
		}
		if (hasKey) {
			this.plugin.getConfigManager().getConfig().set("UUID", null);
			this.plugin.getConfigManager().save();
		}
		hasKey = false;
		for (String key : this.plugin.getConfigManager().getConfig().getSection("Name").getKeys()) {
			hasKey = true;
			this.config.set("Name." + key, this.plugin.getConfigManager().getConfig().get("Name." + key));
		}
		if (hasKey) {
			this.plugin.getConfigManager().getConfig().set("Name", null);
			this.plugin.getConfigManager().save();
		}

		this.config.set("Parties", null);
		this.config.set("Plugin", null);

		this.createConfig();
	}

	public UUID getPlayerUniqueId(String name) {
		UUID uuid = null;
		try {
			uuid = UUID.fromString(this.config.getString("Name." + name.toLowerCase()));
		} catch (Exception e) {
		}
		return uuid;
	}

	public String getPlayerName(UUID uuid) {
		return this.config.getString("UUID." + uuid.toString());
	}

	public void save() {
		File config = new File(this.plugin.getDataFolder(), "player.yml");
		Config.saveConfig(this.config, config);
	}

	public void createConfig() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		File config = new File(folder, "player.yml");
		if (!config.exists()) {
			try {
				config.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(config, Charsets.UTF_8);

			this.config.set("UUID.b0bb65a2-832f-4a5d-854e-873b7c4522ed", "SimplyRin");
			this.config.set("Name.simplyrin", "b0bb65a2-832f-4a5d-854e-873b7c4522ed");

			this.config.set("UUID.64636120-8633-4541-aa5f-412b42ddb04d", "SimplyFoxy");
			this.config.set("Name.simplyfoxy", "64636120-8633-4541-aa5f-412b42ddb04d");

			Config.saveConfig(this.config, config);
		}

		this.config = Config.getConfig(config, Charsets.UTF_8);

		this.save();
	}

}
