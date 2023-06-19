package me.snowznz.snowreports.utils;

import org.bukkit.ChatColor;

public class ChatColors {

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
