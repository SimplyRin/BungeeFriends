package net.simplyrin.bungeefriends.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.exceptions.parties.NotJoinedException;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.tools.ThreadPool;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;
import net.simplyrin.bungeefriends.utils.PartyManager.PartyUtils;

/**
 * Created by SimplyRin on 2020/04/30.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class PartyChatCommand extends Command {

	private Main plugin;

	public PartyChatCommand(Main plugin, String cmd) {
		super(cmd, null);
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
		PartyUtils myParties = this.plugin.getPartyManager().getPlayer(player);
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

		if (!myParties.isJoinedParty()) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		}

		if (args.length > 0) {
			String message = "";
			for (int i = 0; i < args.length; i++) {
				message += args[i] + " ";
			}
			message = message.trim();

			PartyUtils partyLeader;
			try {
				partyLeader = myParties.getPartyLeader();
			} catch (NotJoinedException e) {
				return;
			}

			try {
				partyLeader.partyChat(player, message);
			} catch (NotJoinedException e) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Chat.NeedInAParty"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			}
			return;
		}

		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
		this.plugin.info(player, langUtils.getString("Chat.ChatUsage"));
		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
		return;
	}

}
