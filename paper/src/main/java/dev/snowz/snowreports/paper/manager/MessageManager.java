package dev.snowz.snowreports.paper.manager;

import dev.snowz.snowreports.paper.SnowReports;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Objects;

public final class MessageManager {

    private static final SnowReports plugin = SnowReports.getInstance();

    private static FileConfiguration messagesConfig;
    private static File messagesFile;

    public static void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static String getDefaultMessage(final String path) {
        final InputStream defaultMessagesResource = plugin.getResource("messages.yml");
        if (defaultMessagesResource != null) {
            final FileConfiguration defaultMessagesConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(
                defaultMessagesResource));
            return defaultMessagesConfig.getString(path, "\"" + path + "\" not found");
        } else {
            return null;
        }
    }

    public static Component getMessage(final String path) {
        return getMessage(path, "");
    }

    public static Component getMessage(final String path, final Object... args) {
        String stringMessage = MessageFormat.format(
            Objects.requireNonNull(messagesConfig.getString(
                path,
                getDefaultMessage(path)
            )), args
        );
        final String prefixMessage = MessageFormat.format(
            Objects.requireNonNull(messagesConfig.getString("prefix", getDefaultMessage("prefix"))),
            args
        );

        final Component prefix;
        if (stringMessage.contains("<no-prefix>")) {
            stringMessage = stringMessage.replace("<no-prefix>", "");
            prefix = Component.empty();
        } else {
            prefix = MiniMessage.miniMessage().deserialize(prefixMessage);
        }

        final Component message = MiniMessage.miniMessage().deserialize(stringMessage);

        return prefix.append(message);
    }

    public static Component deserialize(final String input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }

    public static void reload() {
        if (messagesFile == null) {
            loadMessages();
            return;
        }

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
}
