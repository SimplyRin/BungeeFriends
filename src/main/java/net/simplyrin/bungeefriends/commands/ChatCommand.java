package net.simplyrin.bungeefriends.commands;

import java.util.HashMap;
import java.util.UUID;

import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.messages.Permissions;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;

/**
 * Created by SimplyRin on 2020/04/30.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class ChatCommand extends Command {

	private Main plugin;
	@Getter
	private HashMap<UUID, Channel> channels = new HashMap<>();

	public ChatCommand(Main plugin, String cmd) {
		super(cmd, null);
		this.plugin = plugin;
	}

	public enum Channel {
		ALL, PARTY, STAFF
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			this.plugin.info(Messages.INGAME_ONLY);
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("default")) {
				this.channels.remove(player.getUniqueId());
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Chat.Update").replace("%CHANNEL%", langUtils.getString("Chat.Channels.All")));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}
			if (args[0].equalsIgnoreCase("party") || args[0].equalsIgnoreCase("p")) {
				this.channels.put(player.getUniqueId(), Channel.PARTY);
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Chat.Update").replace("%CHANNEL%", langUtils.getString("Chat.Channels.Party")));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}
			if (args[0].equalsIgnoreCase("staff") || args[0].equalsIgnoreCase("s") && player.hasPermission(Permissions.ADMIN)) {
				this.channels.put(player.getUniqueId(), Channel.STAFF);
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Chat.Update").replace("%CHANNEL%", langUtils.getString("Chat.Channels.Staff")));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}
		}

		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
		this.plugin.info(player, langUtils.getString("Chat.Usage"));
		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
	}

}
