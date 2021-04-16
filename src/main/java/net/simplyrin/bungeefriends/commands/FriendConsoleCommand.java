package net.simplyrin.bungeefriends.commands;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Charsets;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.tools.Config;
import net.simplyrin.bungeefriends.tools.ThreadPool;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;

/**
 * Created by SimplyRin on 2021/02/12.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class FriendConsoleCommand extends Command {

	private Main plugin;

	public FriendConsoleCommand(Main plugin, String cmd) {
		super(cmd, null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ThreadPool.run(() -> this.async(sender, args));
	}

	public void async(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			this.plugin.info(Messages.CONSOLE_ONLY);
			return;
		}

		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(this.plugin.getConsoleUniqueId());

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("prefix")) {
				if (args.length > 1) {
					UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(langUtils.getString(Messages.HYPHEN));
						this.plugin.info(langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(langUtils.getString(Messages.HYPHEN));
						return;
					}
					if (args.length > 2) {
						FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
						String prefix = "";
						for (int i = 2; i < args.length; i++) {
							prefix = prefix + args[i] + " ";
						}

						if (prefix.trim().length() <= 2) {
							prefix = prefix.trim();
						}
						String displayName = targetFriends.getDisplayName();
						targetFriends.setPrefix(prefix);

						this.plugin.info(langUtils.getString("Prefix.To").replace("%targetDisplayName", displayName)
								.replace("%prefix", ChatColor.translateAlternateColorCodes('&', prefix.trim())));
						return;
					}
				}
				this.plugin.info(langUtils.getString(Messages.HYPHEN));
				this.plugin.info(langUtils.getString("Prefix.Usage").replace("/friend", "/friendconsole"));
				this.plugin.info(langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args[0].equalsIgnoreCase("reload")) {
				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("all")) {
						this.plugin.getConfigManager().reload();
						this.plugin.info(langUtils.getString("Reload.Config"));

						this.plugin.reloadLanguageManager();
						this.plugin.info(langUtils.getString("Reload.Lang"));
						return;
					}
					if (args[1].equalsIgnoreCase("config")) {
						this.plugin.getConfigManager().reload();
						this.plugin.info(langUtils.getString("Reload.Config"));
						return;
					}
					if (args[1].equalsIgnoreCase("lang")) {
						this.plugin.reloadLanguageManager();
						this.plugin.info(langUtils.getString("Reload.Lang"));
						return;
					}
				}

				this.plugin.info(langUtils.getString("Reload.Usage"));
				return;
			}

			if (args[0].equalsIgnoreCase("lang") || args[0].equalsIgnoreCase("language")) {
				File folder = this.plugin.getDataFolder();
				if (!folder.exists()) {
					folder.mkdir();
				}

				File languageFolder = new File(folder, "Language");
				if (!languageFolder.exists()) {
					languageFolder.mkdir();
				}

				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("update")) {
						if (args.length > 2) {
							if (this.plugin.getFriendCommand().getAvailableLanguages().size() == 0) {
								this.plugin.info(langUtils.getString("Lang.Updater.NeedCheck"));
								return;
							}

							String lang = null;
							for (String available : this.plugin.getFriendCommand().getAvailableLanguages()) {
								if (args[2].equalsIgnoreCase(available)) {
									lang = available;
								}
							}

							if (lang == null) {
								this.plugin.info(langUtils.getString("Lang.Updater.Unknown"));
								return;
							}

							final String outputLang = lang;
							ThreadPool.run(() -> {
								Configuration config;
								try {
									config = Config.getConfig(new URL("https://api.simplyrin.net/Bungee-Plugins/BungeeFriends/Languages/Files/" + outputLang + ".yml"));
								} catch (Exception e) {
									this.plugin.info(langUtils.getString("Lang.Updater.Failed-Connect"));
									return;
								}

								File file = new File(languageFolder, outputLang.toLowerCase() + ".yml");
								Config.saveConfig(config, file);

								langUtils.reloadLanguage(outputLang.toLowerCase());
								this.plugin.info(langUtils.getString("Lang.Updater.Updated").replace("%lang", outputLang));
							});
							return;
						}

						ThreadPool.run(() -> {
							this.plugin.info(langUtils.getString("Lang.Updater.Checking"));
							Configuration config;
							try {
								config = Config.getConfig(new URL("https://api.simplyrin.net/Bungee-Plugins/BungeeFriends/Languages/available.txt"));
							} catch (Exception e) {
								e.printStackTrace();
								this.plugin.info(langUtils.getString("Lang.Updater.Failed-Connect"));
								return;
							}

							this.plugin.getFriendCommand().getAvailableLanguages().clear();

							this.plugin.info(langUtils.getString("Lang.Updater.LastUpdate").replace("%data", config.getString("LastUpdate")));
							this.plugin.info(langUtils.getString("Lang.Updater.Available"));
							for (String lang : config.getStringList("Langs")) {
								this.plugin.info("&b- " + lang + " &a(" + config.getString("Percent." + lang) + ")");
								this.plugin.getFriendCommand().getAvailableLanguages().add(lang);
							}
						});

						this.plugin.info(langUtils.getString("Lang.Updater.Usage").replace("/friend", "/friendconsole"));
						return;
					}
				}

				List<String> availableList = new ArrayList<>();
				String available = "";
				File[] languages = languageFolder.listFiles();
				for (File languageFile : languages) {
					Configuration langConfig = Config.getConfig(languageFile, Charsets.UTF_8);
					if (langConfig.getString("Language").length() > 1) {
						availableList.add(languageFile.getName().toLowerCase().replace(".yml", ""));
						available += langConfig.getString("Language") + ", ";
					}
				}

				if (args.length > 1) {
					if (availableList.contains(args[1].toLowerCase())) {
						langUtils.setLanguage(args[1].toLowerCase());
						this.plugin.info(langUtils.getString(Messages.HYPHEN));
						this.plugin.info(langUtils.getString("Lang.Update").replace("%lang", langUtils.getLanguage().replace("/friend", "/friendconsole")));
						this.plugin.info(langUtils.getString(Messages.HYPHEN));
						return;
					}
				}

				this.plugin.info(langUtils.getString(Messages.HYPHEN));
				this.plugin.info(langUtils.getString("Lang.Usage").replace("/friend", "/friendconsole"));
				this.plugin.info(langUtils.getString("Lang.Available").replace("/friend", "/friendconsole") + " <" + available.substring(0, available.length() - 1) + ">");
				this.plugin.info(langUtils.getString(Messages.HYPHEN));
				return;
			}
		}

		this.plugin.info(langUtils.getString(Messages.HYPHEN));
		this.plugin.info(langUtils.getString("Help.Lang").replace("/friend", "/friendconsole"));
		this.plugin.info(langUtils.getString("Help.Prefix").replace("/friend", "/friendconsole"));
		this.plugin.info(langUtils.getString("Help.Reload").replace("/friend", "/friendconsole"));
		this.plugin.info(langUtils.getString(Messages.HYPHEN));
		return;
	}

}
