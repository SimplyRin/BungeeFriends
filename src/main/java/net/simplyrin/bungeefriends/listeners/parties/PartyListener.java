package net.simplyrin.bungeefriends.listeners.parties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.commands.ChatCommand.Channel;
import net.simplyrin.bungeefriends.exceptions.parties.NotJoinedException;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.tools.ThreadPool;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;
import net.simplyrin.bungeefriends.utils.PartyManager.PartyUtils;

/**
 * Created by SimplyRin on 2018/07/31.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class PartyListener implements Listener {

	private Main plugin;

	public PartyListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();

		this.plugin.getPartyManager().getPlayer(player);

		this.plugin.set("Parties." + player.getUniqueId().toString() + ".Name", player.getName());

		this.plugin.set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
		this.plugin.set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());
	}

	@EventHandler
	public void onChat(ChatEvent event) {
		if (!(event.getSender() instanceof ProxiedPlayer)) {
			return;
		}

		if (event.isCommand()) {
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) event.getSender();

		Channel channel = this.plugin.getChatCommand().getChannels().get(player.getUniqueId());
		if (channel == null) {
			return;
		}

		if (channel.equals(Channel.PARTY)) {
			PartyUtils myParties = this.plugin.getPartyManager().getPlayer(player);
			if (!myParties.isJoinedParty()) {
				return;
			}

			PartyUtils partyLeader;
			try {
				partyLeader = myParties.getPartyLeader();
			} catch (NotJoinedException e) {
				this.plugin.getChatCommand().getChannels().remove(player.getUniqueId());
				return;
			}

			event.setCancelled(true);
			try {
				partyLeader.partyChat(player, event.getMessage());
			} catch (NotJoinedException e) {
			}
			return;
		}
	}

	@EventHandler
	public void partyCmdSender(ChatEvent event) {
		if (!event.isCommand()) {
			return;
		}

		// Plugin.BungeeParties.PartyCmdSender

		String cmd = event.getMessage();

		for (Entry<String, String> entry : this.plugin.getPartyCmdSenderMap().entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			if (cmd.startsWith(key)) {
				String gameId = cmd.replace(key, "").trim();
				String memberSend = value.replace("$args", gameId);

				ProxiedPlayer player = (ProxiedPlayer) event.getSender();

				PartyUtils myParties = this.plugin.getPartyManager().getPlayer(player);
				PartyUtils partyLeader;
				try {
					partyLeader = myParties.getPartyLeader();
				} catch (NotJoinedException e) {
					return;
				}
				boolean isPartyLeader = player.getUniqueId().toString().equals(partyLeader.getUniqueId().toString());
				if (!isPartyLeader) {
					return;
				}

				for (String stringUniqueId : partyLeader.getParties()) {
					UUID uniqueId = UUID.fromString(stringUniqueId);
					ProxiedPlayer partyMember = this.plugin.getProxy().getPlayer(uniqueId);
					if (partyMember != null) {
						partyMember.chat(memberSend);
					}
				}
				break;
			}
		}
	}

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		PartyUtils myParties = this.plugin.getPartyManager().getPlayer(player);

		if (myParties == null) {
			return;
		}

		if (myParties.getParties().size() == 0) {
			return;
		}

		try {
			if (!myParties.isPartyOwner()) {
				return;
			}
		} catch (NotJoinedException e) {
			return;
		}

		ThreadPool.run(() -> {
			for (String partyPlayerUniqueId : myParties.getParties()) {
				PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
				LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayerUniqueId);

				this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));

				this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString("Logout.One").replace("%displayName", myParties.getDisplayName()));
				this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString("Logout.Two"));
				this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
			}

			try {
				TimeUnit.MINUTES.sleep(5);
				// TimeUnit.SECONDS.sleep(5);
			} catch (Exception e) {
			}

			if (myParties.getPlayer() != null) {
				return;
			}

			if (myParties.getOnlinePlayers() >= 2) {
				String uniqueId = myParties.getParties().get(0);
				myParties.getParties().remove(0);

				try {
					myParties.promote(UUID.fromString(uniqueId));
					PartyUtils newParties = this.plugin.getPartyManager().getPlayer(uniqueId);

					for (String partyPlayerUniqueId : newParties.getParties()) {
						PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
						LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayerUniqueId);

						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString("Logout.Become").replace("%displayName", newParties.getDisplayName()));
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
					}

					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(uniqueId);
					this.plugin.info(newParties.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
					this.plugin.info(newParties.getPlayer(), targetLangUtils.getString("Logout.Become").replace("%displayName", newParties.getDisplayName()));
					this.plugin.info(newParties.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
				} catch (NotJoinedException e) {
				}
			} else {
				for (String partyPlayerUniqueId : myParties.getParties()) {
					PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayerUniqueId);

					this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
					this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString("Disband.Disbanded").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));

					this.plugin.set("Parties." + targetPlayer.getUniqueId() + ".Currently-Joined-Party", "NONE");
					this.plugin.set("Parties." + targetPlayer.getUniqueId() + ".Party-List", new ArrayList<>());
					this.plugin.set("Parties." + targetPlayer.getUniqueId() + ".Requests", new ArrayList<>());
				}
			}
		});
	}

	@EventHandler
	public void onSwitch(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		PartyUtils partyUtils = this.plugin.getPartyManager().getPlayer(player);

		try {
			if (!partyUtils.isPartyOwner()) {
				return;
			}
		} catch (NotJoinedException e) {
			return;
		}

		String serverName = player.getServer().getInfo().getName().toLowerCase();
		if (serverName == null || serverName.equals("")) {
			return;
		}
		if (serverName.contains(this.plugin.getString("Plugin.Bypass-Lobby-Name-Contains").toLowerCase())) {
			return;
		}

		if (this.plugin.getBoolean("Plugin.Disable-PartyWarp")) {
			return;
		}

		List<String> parties = partyUtils.getParties();
		for (String partyPlayerUniqueId : parties) {
			ProxiedPlayer targetPlayer = this.plugin.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
			if (targetPlayer != null) {
				LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetPlayer);

				String name = this.plugin.getServerName(player.getServer().getInfo().getName());

				this.plugin.info(targetPlayer, targetLangUtils.getString("Warp.Sending").replace("%server%", name));
				targetPlayer.connect(player.getServer().getInfo());
			}
		}
	}

}
