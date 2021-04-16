package net.simplyrin.bungeefriends;

import java.util.UUID;

import lombok.Setter;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.PartyManager.PartyUtils;

/**
 * Created by SimplyRin on 2021/02/21.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class BungeeFriends {

	private static BungeeFriends priv;

	public static BungeeFriends getAPI() {
		if (priv == null) {
			priv = new BungeeFriends();
		}
		return priv;
	}

	@Setter
	protected Main instance;

	public FriendUtils getFriendUtils(UUID uniqueId) {
		return this.instance.getFriendManager().getPlayer(uniqueId);
	}

	public PartyUtils getPartyUtils(UUID uniqueId) {
		return this.instance.getPartyManager().getPlayer(uniqueId);
	}

}
