package net.simplyrin.bungeefriends.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.exceptions.parties.NotInvitedException;
import net.simplyrin.bungeefriends.exceptions.parties.NotJoinedException;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.messages.Permissions;
import net.simplyrin.bungeefriends.tools.ThreadPool;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;
import net.simplyrin.bungeefriends.utils.MessageBuilder;
import net.simplyrin.bungeefriends.utils.PartyManager.PartyUtils;

/**
 * Created by SimplyRin on 2018/07/31.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class PartyCommand extends Command {

	private Main plugin;

	public PartyCommand(Main plugin) {
		super("party", null, "p");
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

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("invite")) {
				if (args.length > 1) {
					this.invite(player, myParties, args[1]);
					return;
				}
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Invite.Usage"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args[0].equalsIgnoreCase("leave")) {
				try {
					if (myParties.isPartyOwner()) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Leave.NeedDisband"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
				} catch (Exception e) {
				}

				if (!myParties.isJoinedParty()) {
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}

				PartyUtils partyLeader = myParties.leaveCurrentParty();
				LanguageUtils partyLeaderLangUtils = this.plugin.getLanguageManager().getPlayer(partyLeader.getUniqueId());

				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Leave.You-Left"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

				if (myParties.getParties().size() > 0) {
					this.plugin.info(partyLeader.getPlayer(), partyLeaderLangUtils.getString(Messages.HYPHEN));
					this.plugin.info(partyLeader.getPlayer(), partyLeaderLangUtils.getString("Leave.Player-Left").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(partyLeader.getPlayer(), partyLeaderLangUtils.getString(Messages.HYPHEN));

					for (String partyPlayerUniqueId : myParties.getParties()) {
						ProxiedPlayer partyPlayer = this.plugin.getProxy().getPlayer(partyPlayerUniqueId);
						LanguageUtils partyPlayerLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayerUniqueId);
						this.plugin.info(partyPlayer, partyPlayerLangUtils.getString(Messages.HYPHEN));
						this.plugin.info(partyPlayer, partyPlayerLangUtils.getString("Leave.Player-Left").replace("%displayName", myParties.getDisplayName()));
						this.plugin.info(partyPlayer, partyPlayerLangUtils.getString(Messages.HYPHEN));
					}
				}
				return;
			}

			if (args[0].equalsIgnoreCase("list")) {
				if (!myParties.isJoinedParty()) {
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}

				PartyUtils partyLeader;
				try {
					partyLeader = myParties.getPartyLeader();
				} catch (NotJoinedException e) {
					return;
				}

				List<String> parties = partyLeader.getParties();

				String raw = "";
				raw += partyLeader.getDisplayName() + (partyLeader.getPlayer() != null ? " &a● " : " &c● ");

				for (String partyPlayerUniqueId : parties) {
					PartyUtils member = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
					raw += member.getDisplayName() + (member.getPlayer() != null ? " &a● " : " &c● ");
				}

				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("List.Party-List").replace("%size", String.valueOf(parties.size() + 1)).trim() + " " + raw);
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("kick")) {
				if (args.length > 1) {
					if (!myParties.isJoinedParty()) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
					PartyUtils targetParties = this.plugin.getPartyManager().getPlayer(target);

					try {
						myParties.remove(target);
					} catch (NotJoinedException e) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString(e.getMessage()).replace("%targetDisplayname", targetParties.getDisplayName()));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("Remove-Party.Member-Removed").replace("%targetDisplayName", targetParties.getDisplayName()));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

					if (myParties.getParties().size() > 0) {
						for (String partyPlayerUniqueId : myParties.getParties()) {
							ProxiedPlayer partyPlayer = this.plugin.getProxy().getPlayer(partyPlayerUniqueId);
							LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayerUniqueId);

							this.plugin.info(partyPlayer, langUtils.getString(Messages.HYPHEN));
							this.plugin.info(partyPlayer, targetLangUtils.getString("Remove-Party.Member-Removed").replace("%targetDisplayName", targetParties.getDisplayName()));
							this.plugin.info(partyPlayer, langUtils.getString(Messages.HYPHEN));
						}
					}

					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

					this.plugin.info(target, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(target, targetLangUtils.getString("Remove-Party.Me-Removed").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(target, langUtils.getString(Messages.HYPHEN));
					return;
				}
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Remove-Party.Usage"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args[0].equalsIgnoreCase("promote")) {
				try {
					if (!myParties.isPartyOwner()) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Disband.Must-Leader"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
				} catch (NotJoinedException e) {
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString(e.getMessage()));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}

				if (args.length > 1) {
					UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
					PartyUtils newPartyLeader = this.plugin.getPartyManager().getPlayer(target);
					UUID uniqueId = newPartyLeader.getUniqueId();

					try {
						myParties.promote(uniqueId);
					} catch (NotJoinedException e) {
						e.printStackTrace();
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString(e.getMessage()));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					}

					String displayName = myParties.getDisplayName();
					myParties = this.plugin.getPartyManager().getPlayer(target);

					for (String partyPlayerUniqueId : myParties.getParties()) {
						PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
						LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayerUniqueId);

						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString("Promote.Promoted").replace("%displayName", displayName).replace("%targetDisplayName", newPartyLeader.getDisplayName()));
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
					}

					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(newPartyLeader.getUniqueId());
					this.plugin.info(myParties.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
					this.plugin.info(myParties.getPlayer(), targetLangUtils.getString("Promote.Promoted").replace("%displayName", displayName).replace("%targetDisplayName", newPartyLeader.getDisplayName()));
					this.plugin.info(myParties.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
					return;
				}

				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Promote.Usage"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args[0].equalsIgnoreCase("hijack")) {
				if (!player.hasPermission(Permissions.ADMIN)) {
					this.plugin.info(player, langUtils.getString(Messages.NO_PERMISSION));
					return;
				}

				if (args.length > 1) {
					UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					PartyUtils targetParty = this.plugin.getPartyManager().getPlayer(target);
					try {
						if (!targetParty.isPartyOwner()) {
							targetParty = targetParty.getPartyLeader();
						}
					} catch (Exception e) {
					}

					if (targetParty.getParties().size() == 0) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Hijack.PlayerNotJoined"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					targetParty.hijack(player);

					for (String partyPlayerUniqueId : targetParty.getParties()) {
						PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
						LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayerUniqueId);

						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString("Hijack.Other").replace("%displayName", myParties.getDisplayName()));
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
					}

					this.plugin.info(myParties.getPlayer(), langUtils.getString(Messages.HYPHEN));
					this.plugin.info(myParties.getPlayer(), langUtils.getString("Hijack.You"));
					this.plugin.info(myParties.getPlayer(), langUtils.getString(Messages.HYPHEN));

					return;
				}

				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Hijack.Usage"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args[0].equalsIgnoreCase("warp")) {
				try {
					if (!myParties.isPartyOwner()) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Disband.Must-Leader"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
				} catch (NotJoinedException e) {
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}

				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Warp.Summon").replace("%displayName", myParties.getDisplayName()));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

				for (String partyPlayerUniqueId : myParties.getParties()) {
					ProxiedPlayer targetPlayer = this.plugin.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
					if (targetPlayer != null) {
						targetPlayer.connect(myParties.getPlayer().getServer().getInfo());

						LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetPlayer);

						this.plugin.info(targetPlayer, targetLangUtils.getString(Messages.HYPHEN));
						this.plugin.info(targetPlayer, targetLangUtils.getString("Warp.Summon").replace("%displayName", myParties.getDisplayName()));
						this.plugin.info(targetPlayer, targetLangUtils.getString(Messages.HYPHEN));
					}
				}
				return;
			}

			if (args[0].equalsIgnoreCase("accept")) {
				if (args.length > 1) {
					UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[1]);
					if (target == null) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
					PartyUtils targetParties = this.plugin.getPartyManager().getPlayer(target);

					try {
						targetParties.removeRequest(player);
					} catch (NotInvitedException e) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString(e.getMessage()));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}

					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("Accept-Party.You-Joined").replace("%targetDisplayName", targetParties.getDisplayName()));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetParties.getPlayer());

					this.plugin.info(targetParties.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
					this.plugin.info(targetParties.getPlayer(), targetLangUtils.getString("Accept-Party.Joined").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(targetParties.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));

					for (String partyPlayerUniqueId : targetParties.getParties()) {
						PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
						targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetPlayer.getPlayer());

						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString("Accept-Party.Joined").replace("%displayName", myParties.getDisplayName()));
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString(Messages.HYPHEN));
					}

					try {
						targetParties.add(player);
					} catch (Exception e) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString(e.getMessage()).replace("%targetDisplayName", targetParties.getDisplayName()));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					}
					return;
				}
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Accept-Party.Usage"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args[0].equalsIgnoreCase("toggle")) {
				boolean bool = myParties.isEnabledReceiveRequest();
				if (bool) {
					myParties.setEnabledReceiveRequest(false);
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("Toggle-Party.Disabled"));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}
				myParties.setEnabledReceiveRequest(true);
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Toggle-Party.Enabled"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			if (args[0].equalsIgnoreCase("disband")) {
				try {
					if (!myParties.isPartyOwner()) {
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						this.plugin.info(player, langUtils.getString("Disband.Must-Leader"));
						this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
						return;
					}
				} catch (NotJoinedException e) {
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString(e.getMessage()));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}

				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Disband.Disbanded").replace("%displayName", myParties.getDisplayName()));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

				for (String partyPlayerUniqueId : myParties.getParties()) {
					PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetPlayer.getUniqueId());

					this.plugin.info(targetPlayer.getUniqueId(), targetLangUtils.getString(Messages.HYPHEN));
					this.plugin.info(targetPlayer.getUniqueId(), targetLangUtils.getString("Disband.Disbanded").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(targetPlayer.getUniqueId(), targetLangUtils.getString(Messages.HYPHEN));

					this.plugin.set("Parties." + targetPlayer.getUniqueId() + ".Currently-Joined-Party", "NONE");
					this.plugin.set("Parties." + targetPlayer.getUniqueId() + ".Party-List", new ArrayList<>());
				}

				this.plugin.set("Parties." + myParties.getUniqueId() + ".Currently-Joined-Party", "NONE");
				this.plugin.set("Parties." + myParties.getUniqueId() + ".Party-List", new ArrayList<>());
				return;
			}

			if (args[0].equalsIgnoreCase("kickoffline")) {
				if (!myParties.isJoinedParty()) {
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					return;
				}

				PartyUtils partyLeader;
				try {
					partyLeader = myParties.getPartyLeader();
				} catch (NotJoinedException e) {
					e.printStackTrace();
					return;
				}
				if (!partyLeader.getUniqueId().equals(myParties.getUniqueId())) {
					return;
				}

				boolean found = false;

				for (String stringUniqueId : partyLeader.getParties()) {
					UUID uniqueId = UUID.fromString(stringUniqueId);
					ProxiedPlayer member = this.plugin.getProxy().getPlayer(uniqueId);
					if (member == null) {
						try {
							partyLeader.remove(uniqueId);

							String displayName = plugin.getFriendManager().getPlayer(uniqueId).getDisplayName();

							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
							this.plugin.info(player, langUtils.getString("Remove-Party.Member-Removed").replace("%targetDisplayName", displayName));
							this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

							for (String partyPlayerUniqueId : myParties.getParties()) {
								ProxiedPlayer partyPlayer = this.plugin.getProxy().getPlayer(partyPlayerUniqueId);
								LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayerUniqueId);

								this.plugin.info(partyPlayer, targetLangUtils.getString(Messages.HYPHEN));
								this.plugin.info(partyPlayer, targetLangUtils.getString("Remove-Party.Member-Removed").replace("%targetDisplayName", displayName));
								this.plugin.info(partyPlayer, targetLangUtils.getString(Messages.HYPHEN));
							}
							found = true;
						} catch (NotJoinedException e) {
						}
					}
				}
				if (partyLeader.getParties().isEmpty()) {
					plugin.set("Parties." + myParties.getUniqueId().toString() + ".Currently-Joined-Party", "NONE");
					plugin.set("Parties." + myParties.getUniqueId().toString() + ".Party-List", new ArrayList<>());
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("Disband.Disbanded").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				}
				if (!found) {
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
					this.plugin.info(player, langUtils.getString("KickOffline.AllOnline"));
					this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				}
				return;
			}

			if (args[0].equalsIgnoreCase("help")) {
				this.printHelp(player);
				return;
			}

			if (args[0].equalsIgnoreCase("chat")) {
				if (args.length > 1) {
					String message = "";
					for (int i = 1; i < args.length; i++) {
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
						return;
					}
					return;
				}
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Chat.ChatUsage"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}

			this.invite(player, myParties, args[0]);
			return;
		}
		this.printHelp(player);
	}

	private void printHelp(ProxiedPlayer player) {
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
		this.plugin.info(player, langUtils.getString("Help-Party.Command"));
		this.plugin.info(player, langUtils.getString("Help-Party.Help"));
		this.plugin.info(player, langUtils.getString("Help-Party.Chat"));
		this.plugin.info(player, langUtils.getString("Help-Party.Invite"));
		this.plugin.info(player, langUtils.getString("Help-Party.Leave"));
		this.plugin.info(player, langUtils.getString("Help-Party.List"));
		this.plugin.info(player, langUtils.getString("Help-Party.Remove"));
		this.plugin.info(player, langUtils.getString("Help-Party.Warp"));
		this.plugin.info(player, langUtils.getString("Help-Party.Accept"));
		this.plugin.info(player, langUtils.getString("Help-Party.Toggle"));
		this.plugin.info(player, langUtils.getString("Help-Party.Disband"));
		this.plugin.info(player, langUtils.getString("Help-Party.Promote"));
		this.plugin.info(player, langUtils.getString("Help-Party.KickOffline"));
		if (player.hasPermission(Permissions.ADMIN)) {
			this.plugin.info(player, langUtils.getString("Help-Party.Hijack"));
		}
		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
	}

	private void invite(ProxiedPlayer player, PartyUtils myParties, String args) {
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

		UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args);
		if (target == null) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		}
		PartyUtils targetParties = this.plugin.getPartyManager().getPlayer(target);
		LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

		try {
			if (!myParties.isPartyOwner()) {
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				this.plugin.info(player, langUtils.getString("Disband.Must-Leader"));
				this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
				return;
			}
		} catch (NotJoinedException e) {
		}

		if (targetParties.getPlayer() == null) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("Tell-Command.Offline").replace("%targetDisplayName", targetParties.getDisplayName()));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		}

		try {
			myParties.addRequest(target);
		} catch (Exception e) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString(e.getMessage()).replace("%targetDisplayName", targetParties.getDisplayName()));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		}

		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
		this.plugin.info(player, langUtils.getString("Invite.Request.Sent.Invited").replace("%displayName", myParties.getDisplayName()).replace("%targetDisplayName", targetParties.getDisplayName()));
		this.plugin.info(player, langUtils.getString("Invite.Request.Sent.60-Seconds"));
		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));

		for (String partyPlayerUniqueId : myParties.getParties()) {
			UUID uniqueId = UUID.fromString(partyPlayerUniqueId);
			LanguageUtils pLangUtils = this.plugin.getLanguageManager().getPlayer(uniqueId);
			this.plugin.info(uniqueId, pLangUtils.getString(Messages.HYPHEN));
			this.plugin.info(uniqueId, pLangUtils.getString("Invite.Request.Sent.Invited").replace("%displayName", myParties.getDisplayName()).replace("%targetDisplayName", targetParties.getDisplayName()));
			this.plugin.info(uniqueId, pLangUtils.getString(Messages.HYPHEN));
		}

		this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
		this.plugin.info(target, targetLangUtils.getString("Invite.Request.Click-here.Receive").replace("%displayName", myParties.getDisplayName()));
		if (targetParties.getPlayer() != null) {
			TextComponent prefix = MessageBuilder.get(this.plugin.getPrefix());

			String clickHere = targetLangUtils.getString("Invite.Request.Click-here.Here");
			String clickToRun = targetLangUtils.getString("Invite.Request.Click-here.Command").replace("%name", player.getName());

			TextComponent invite = MessageBuilder.get(clickHere, "/party accept " + player.getName(), ChatColor.GOLD, clickToRun, false);
			TextComponent message = MessageBuilder.get(targetLangUtils.getString("Invite.Request.Click-here.Accept"), "/party accept " + player.getName(), ChatColor.YELLOW, clickToRun, false);
			targetParties.getPlayer().sendMessage(prefix, invite, message);
		}
		this.plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));

		ThreadPool.run(() -> {
			try {
				TimeUnit.MINUTES.sleep(1);
			} catch (Exception e) {
			}

			try {
				myParties.removeRequest(target);
			} catch (NotInvitedException e) {
				return;
			}

			plugin.info(player, langUtils.getString(Messages.HYPHEN));
			plugin.info(player, langUtils.getString("Invite.Request.Expired.Your-Self").replace("%targetDisplayName", targetParties.getDisplayName()));
			plugin.info(player, langUtils.getString(Messages.HYPHEN));

			if (myParties.getParties().size() == 0) {
				plugin.info(player, langUtils.getString(Messages.HYPHEN));
				plugin.info(player, langUtils.getString("Invite.All-Left"));
				plugin.info(player, langUtils.getString(Messages.HYPHEN));
			}

			plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
			plugin.info(target, targetLangUtils.getString("Invite.Request.Expired.Target").replace("%displayName", myParties.getDisplayName()));
			plugin.info(target, targetLangUtils.getString(Messages.HYPHEN));
		});
	}

}
