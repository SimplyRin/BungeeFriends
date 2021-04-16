package net.simplyrin.bungeefriends.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.exceptions.parties.AlreadyJoinedException;
import net.simplyrin.bungeefriends.exceptions.parties.FailedAddingException;
import net.simplyrin.bungeefriends.exceptions.parties.FailedInvitingException;
import net.simplyrin.bungeefriends.exceptions.parties.NotInvitedException;
import net.simplyrin.bungeefriends.exceptions.parties.NotJoinedException;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;

/**
 * Created by SimplyRin on 2018/07/31.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class PartyManager {

	private Main plugin;

	public PartyManager(Main plugin) {
		this.plugin = plugin;
	}

	public PartyUtils getPlayer(ProxiedPlayer player) {
		return new PartyUtils(player.getUniqueId());
	}

	public PartyUtils getPlayer(String uuid) {
		return new PartyUtils(UUID.fromString(uuid));
	}

	public PartyUtils getPlayer(UUID uniqueId) {
		return new PartyUtils(uniqueId);
	}

	public class PartyUtils {

		private UUID uuid;

		public PartyUtils(UUID uuid) {
			this.uuid = uuid;

			if (plugin.getString("Parties." + this.uuid.toString() + ".Name").equals("")) {
				ProxiedPlayer player = plugin.getProxy().getPlayer(this.uuid);

				plugin.info("Creating data for player " + player.getName() + "...");

				plugin.set("Parties." + this.uuid.toString() + ".Name", player.getName());
				plugin.set("Parties." + this.uuid.toString() + ".Currently-Joined-Party", "NONE");
				plugin.set("Parties." + this.uuid.toString() + ".Party-List", new ArrayList<>());
				plugin.set("Parties." + this.uuid.toString() + ".Requests", new ArrayList<>());
				plugin.set("Parties." + this.uuid.toString() + ".Toggle", true);
				plugin.getPlayerManager().save();
			}
		}

		public ProxiedPlayer getPlayer() {
			try {
				return plugin.getProxy().getPlayer(this.uuid);
			} catch (Exception e) {
			}
			return null;
		}

		public void broadcast(String text) {
			for (String player : this.getParties()) {
				plugin.info(player, text);
			}
			plugin.info(uuid, text);
		}

		public void partyChat(ProxiedPlayer player, String text) throws NotJoinedException {
			FriendUtils myFriends = plugin.getFriendManager().getPlayer(player);
			PartyUtils myParties = this.getPartyLeader();

			LanguageUtils langUtils = plugin.getLanguageManager().getPlayer(this.uuid);
			plugin.info(myParties.getUniqueId(), langUtils.getString("Chat.Channels.Party") + " > " + myFriends.getDisplayName() + "&f: " + text);

			for (String uniqueId : myParties.getParties()) {
				PartyUtils member = plugin.getPartyManager().getPlayer(uniqueId);
				LanguageUtils targetLangUtils = plugin.getLanguageManager().getPlayer(member.getUniqueId());
				plugin.info(member.getPlayer(), targetLangUtils.getString("Chat.Channels.Party") + " > " + myFriends.getDisplayName() + "&f: " + text);
			}
		}

		/**
		 * @return Party leader
		 */
		public PartyUtils leaveCurrentParty() {
			PartyUtils leader = plugin.getPartyManager().getPlayer(this.getCurrentlyJoinedParty());
			try {
				leader.remove(this.uuid);
			} catch (NotJoinedException e) {
			}
			if (leader.getParties().size() == 0) {
				plugin.set("Parties." + this.uuid.toString() + ".Currently-Joined-Party", "NONE");
				plugin.set("Parties." + leader.getUniqueId().toString() + ".Currently-Joined-Party", "NONE");

				plugin.set("Parties." + uuid.toString() + ".Party-List", new ArrayList<>());
				plugin.set("Parties." + leader.getUniqueId().toString() + ".Party-List", new ArrayList<>());
			}
			return leader;
		}

		public boolean isJoinedParty() {
			String currentlyJoinedParty = this.getCurrentlyJoinedParty();
			return !currentlyJoinedParty.equalsIgnoreCase("NONE");
		}

		public PartyUtils getPartyLeader() throws NotJoinedException {
			String currentlyJoinedParty = this.getCurrentlyJoinedParty();
			if (currentlyJoinedParty.equalsIgnoreCase("NONE")) {
				throw new NotJoinedException("Exceptions.Not-Joined");
			}
			return plugin.getPartyManager().getPlayer(currentlyJoinedParty);
		}

		public String getCurrentlyJoinedParty() {
			String currentlyJoinedParty = plugin.getString("Parties." + this.uuid.toString() + ".Currently-Joined-Party");
			if (currentlyJoinedParty == null || currentlyJoinedParty.equals("")) {
				plugin.set("Parties." + this.uuid.toString() + ".Currently-Joined-Party", "NONE");
			}
			return currentlyJoinedParty;
		}

		public boolean isPartyOwner() throws NotJoinedException {
			return this.uuid == this.getPartyLeader().getPlayer().getUniqueId();
		}

		public PartyUtils setEnabledReceiveRequest(boolean bool) {
			plugin.set("Parties." + this.uuid.toString() + ".Toggle", bool);
			plugin.getPlayerManager().save();
			return this;
		}

		public boolean isEnabledReceiveRequest() {
			return plugin.getBoolean("Parties." + this.uuid.toString() + ".Toggle");
		}

		public UUID getUniqueId() {
			return this.uuid;
		}

		public String getDisplayName() {
			return plugin.getFriendManager().getPlayer(this.uuid).getDisplayName();
		}

		public void promote(ProxiedPlayer player) throws NotJoinedException {
			this.promote(player.getUniqueId());
		}

		public void promote(UUID uniqueId) throws NotJoinedException {
			if (!this.getParties().contains(uniqueId.toString())) {
				throw new NotJoinedException("Exceptions.Isnt-In-Your-Party");
			}

			PartyUtils newOwner = plugin.getPartyManager().getPlayer(uniqueId);
			this.remove(uniqueId);
			for (String partyPlayerUniqueId : this.getParties()) {
				PartyUtils targetPlayer = plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
				try {
					newOwner.add(targetPlayer.getPlayer());
				} catch (Exception e) {
				}
			}

			try {
				newOwner.add(this.uuid);
			} catch (Exception e) {
			}

			for (String partyPlayerUniqueId : newOwner.getParties()) {
				PartyUtils targetPlayer = plugin.getPartyManager().getPlayer(partyPlayerUniqueId);

				plugin.set("Parties." + targetPlayer.getUniqueId() + ".Currently-Joined-Party", uniqueId.toString());
				plugin.set("Parties." + targetPlayer.getUniqueId() + ".Party-List", this.getParties());
			}

			plugin.set("Parties." + uniqueId.toString() + ".Currently-Joined-Party", uniqueId.toString());
			plugin.set("Parties." + uniqueId.toString() + ".Party-List", this.getParties());
		}

		public void hijack(ProxiedPlayer player) {
			this.hijack(player.getUniqueId());
		}

		public void hijack(UUID uniqueId) {
			if (!this.getParties().contains(uniqueId.toString())) {
				try {
					this.add(uniqueId);
				} catch (Exception e) {
				}
			}

			try {
				this.promote(uniqueId);
			} catch (Exception e) {
			}
		}

		public PartyUtils addRequest(ProxiedPlayer player) throws AlreadyJoinedException, FailedInvitingException {
			return this.addRequest(player.getUniqueId());
		}

		public PartyUtils addRequest(UUID uuid) throws AlreadyJoinedException, FailedInvitingException {
			if (this.uuid.toString().equals(uuid.toString())) {
				throw new FailedInvitingException("Exceptions.Cant-Invite-Self");
			}

			if (!plugin.getBoolean("Parties." + uuid.toString() + ".Toggle")) {
				throw new FailedInvitingException("Exceptions.Disable-Invite");
			}

			List<String> list = this.getParties();
			if (list.contains(uuid.toString())) {
				throw new AlreadyJoinedException("Exceptions.Already.In");
			}

			if (!plugin.getString("Parties." + uuid.toString() + ".Currently-Joined-Party").equals("NONE")) {
				throw new AlreadyJoinedException("Exceptions.Already.Joined");
			}

			List<String> requests = plugin.getStringList("Parties." + this.uuid.toString() + ".Requests");
			if (requests.contains(uuid.toString())) {
				throw new FailedInvitingException("Exceptions.Already.Invited");
			}
			requests.add(uuid.toString());
			plugin.set("Parties." + this.uuid.toString() + ".Requests", requests);
			return this;
		}

		public PartyUtils removeRequest(ProxiedPlayer player) throws NotInvitedException {
			return this.removeRequest(player.getUniqueId());
		}

		public PartyUtils removeRequest(UUID uuid) throws NotInvitedException {
			List<String> requests = plugin.getStringList("Parties." + this.uuid.toString() + ".Requests");
			if (!requests.contains(uuid.toString())) {
				throw new NotInvitedException("Exceptions.Havent-Received-Invite");
			}
			requests.remove(uuid.toString());
			plugin.set("Parties." + this.uuid.toString() + ".Requests", requests);
			return this;
		}

		public List<String> getParties() {
			return plugin.getStringList("Parties." + this.uuid.toString() + ".Party-List");
		}

		public int getOnlinePlayers() {
			int i = 0;
			for (String uniqueId : this.getParties()) {
				ProxiedPlayer player = plugin.getProxy().getPlayer(UUID.fromString(uniqueId));
				if (player != null) {
					i++;
				}
			}
			if (this.getPlayer() != null) {
				i++;
			}
			return i;
		}

		public PartyUtils add(ProxiedPlayer player) throws AlreadyJoinedException, FailedAddingException {
			return this.add(player.getUniqueId());
		}

		public PartyUtils add(UUID uuid) throws AlreadyJoinedException, FailedAddingException {
			if (this.uuid.toString().equals(uuid.toString())) {
				throw new FailedAddingException("Exceptions.Your-Self-Invite"); // "You can't add yourself to a party!"
			}

			List<String> list = this.getParties();
			if (list.contains(uuid.toString())) {
				throw new AlreadyJoinedException("Exceptions.Already.In");
						// plugin.getFriendManager().getPlayer(uuid).getDisplayName() + " &cis already in your party!"
			}
			list.add(uuid.toString());
			plugin.set("Parties." + this.uuid.toString() + ".Party-List", list);
			plugin.set("Parties." + this.uuid.toString() + ".Currently-Joined-Party", this.uuid.toString());

			plugin.set("Parties." + uuid.toString() + ".Currently-Joined-Party", this.uuid.toString());
			plugin.set("Parties." + uuid.toString() + ".Party-List", list);
			return this;
		}

		public PartyUtils remove(ProxiedPlayer player) throws NotJoinedException {
			return this.remove(player.getUniqueId());
		}

		public PartyUtils remove(UUID uuid) throws NotJoinedException {
			if (this.uuid.toString().equals(uuid.toString())) {
				throw new NotJoinedException("Exceptions.Cant-Remove-Your-Self"); // "&cYou can't remove yourself! Use /party disaband instead!"
			}

			List<String> list = this.getParties();
			if (!list.contains(uuid.toString())) {
				throw new NotJoinedException("Exceptions.Isnt-In-Your-Party");
						// plugin.getFriendManager().getPlayer(uuid).getDisplayName() + " &cisn't in your party! Invite them first!"
			}
			list.remove(uuid.toString());
			plugin.set("Parties." + this.uuid.toString() + ".Party-List", list);

			plugin.set("Parties." + uuid.toString() + ".Currently-Joined-Party", "NONE");
			plugin.set("Parties." + uuid.toString() + ".Party-List", list);
			return this;
		}

	}

}
