package net.simplyrin.bungeefriends.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.messages.Permissions;
import net.simplyrin.bungeefriends.tools.ThreadPool;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;

/**
 * Created by SimplyRin on 2018/09/14.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class TellCommand extends Command implements TabExecutor {

	private Main plugin;

	public TellCommand(Main plugin, String cmd) {
		super(cmd, null);
		this.plugin = plugin;
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			for (ProxiedPlayer player : this.plugin.getProxy().getPlayers()) {
				if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					list.add(player.getName());
				}
			}
		}
		return list;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ThreadPool.run(() -> this.async(sender, args));
	}

	public void async(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			this.plugin.info(Messages.INGAME_ONLY);
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		FriendUtils myFriends = this.plugin.getFriendManager().getPlayer(player);
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

		if (args.length > 0) {
			UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[0]);
			if (target == null) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[0]));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}
			if (player.getUniqueId().equals(target)) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Tell-Command.CantSend"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}
			FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
			LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

			if (!(myFriends.isFriend(targetFriends.getUniqueId()) || player.hasPermission(Permissions.ADMIN))) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Tell-Command.MustBeFriends").replace("%targetDisplayName", targetFriends.getDisplayName()));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (targetFriends.getPlayer() == null) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Tell-Command.Offline").replace("%targetDisplayName", targetFriends.getDisplayName()));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args.length > 1) {
				String message = "";
				for (int i = 1; i < args.length; i++) {
					message += args[i] + " ";
				}
				message = message.trim();

				this.plugin.info(player, langUtils.getString("Tell-Command.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%message", message));
				this.plugin.info(targetFriends.getPlayer(), targetLangUtils.getString("Tell-Command.Target").replace("%displayName", myFriends.getDisplayName()).replace("%message", message));

				this.plugin.getReplyTargetMap().put(targetFriends.getUniqueId(), player.getUniqueId());
				return;
			}
		}

		this.plugin.info(player, langUtils.getString("Tell-Command.Usage"));
	}

}
