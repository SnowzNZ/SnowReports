package dev.snowz.snowreports;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class L10n {
    private static final int majorVersion = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);

    public static String getMessage(String key, String locale, Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages/messages", new Locale(locale));
        String message = bundle.getString(key);
        return MessageFormat.format(message, args);
    }

    public static String getPlayerLocale(Player player) {
        String locale;

        if (majorVersion < 12) {
            locale = player.spigot().getLocale();
        } else {
            try {
                locale = player.getClass().getMethod("getLocale").invoke(player).toString();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Bukkit.getLogger().warning("Failed to retrieve player locale: " + e.getMessage());
                return "en";
            }
        }

        if (locale.startsWith("en_")) {
            return "en";
        }

        return locale;
    }
}
