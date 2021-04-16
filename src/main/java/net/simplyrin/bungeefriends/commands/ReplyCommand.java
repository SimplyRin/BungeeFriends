package net.simplyrin.bungeefriends.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.messages.Messages;
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
public class ReplyCommand extends Command {

	private Main plugin;

	public ReplyCommand(Main plugin, String command) {
		super(command);
		this.plugin = plugin;
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

		UUID targetUniqueId = this.plugin.getReplyTargetMap().get(myFriends.getUniqueId());
		if (targetUniqueId == null) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Reply-Command.Target").replace("%targetDisplayName", "&cNone!"));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		}

		if (args.length > 0) {
			FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(targetUniqueId);
			LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetUniqueId);

			if (targetFriends.getPlayer() == null) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Tell-Command.Offline").replace("%targetDisplayName", targetFriends.getDisplayName()));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			String message = "";
			for (int i = 0; i < args.length; i++) {
				message = message + args[i] + " ";
			}

			this.plugin.info(player, langUtils.getString("Tell-Command.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%message", message));
			this.plugin.info(targetFriends.getPlayer(), targetLangUtils.getString("Tell-Command.Target").replace("%displayName", myFriends.getDisplayName()).replace("%message", message));

			this.plugin.getReplyTargetMap().put(targetFriends.getUniqueId(), player.getUniqueId());
			return;
		}

		this.plugin.info(player, langUtils.getString("Reply-Command.Usage"));
		if (targetUniqueId != null) {
			FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(targetUniqueId);
			this.plugin.info(player, langUtils.getString("Reply-Command.Target").replace("%targetDisplayName", targetFriends.getDisplayName()));
		}
	}

}
