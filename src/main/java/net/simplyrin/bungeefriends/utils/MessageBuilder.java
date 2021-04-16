package net.simplyrin.bungeefriends.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Created by SimplyRin on 2018/07/03.
 *
 * author: SimplyRin
 * license: LGPL v3
 * copyright: Copyright (c) 2021 SimplyRin
 */
public class MessageBuilder {

	public static TextComponent get(String message) {
		return new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static TextComponent get(String message, String url) {
		TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
		if (url != null) {
			textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
			textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&bClick to open " + url)).create()));
		}
		return textComponent;
	}

	public static TextComponent get(String message, String command, ChatColor color, String hover, boolean bold) {
		TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));

		if (command != null) {
			textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		}
		if (hover != null) {
			textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hover)).create()));
		}
		if (color != null) {
			textComponent.setColor(color);
		}

		textComponent.setBold(bold);
		return textComponent;
	}

}
