package dev.snowz.snowreports.bukkit.command.impl.snowreports;

import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.SnowReports;
import dev.snowz.snowreports.bukkit.command.Command;
import dev.snowz.snowreports.bukkit.command.impl.snowreports.subcommand.*;
import dev.snowz.snowreports.bukkit.command.subcommand.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.snowz.snowreports.bukkit.manager.MessageManager.deserialize;

public final class SnowReportsCommand implements Command {

    private static final Map<String, String> help = new LinkedHashMap<>();

    static {
        help.put("/snowreports help", "Show this help message.");
        help.put("/snowreports alerts (enable/disable)", "Toggle the alerts.");
        help.put("/snowreports enable", "Enable reporting.");
        help.put("/snowreports disable", "Disable reporting.");
        help.put("/snowreports reload (config/messages)", "Reload the config or messages.");
        help.put("/deletereport (id)", "Delete a report.");
        help.put("/report (player) (reason)", "Report a player.");
        help.put("/reports (player) (page)", "View reports.");
    }

    @Override
    public String getName() {
        return "snowreports";
    }

    @Override
    public List<String> getAliases() {
        return List.of("sr");
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> showHelp(sender);
    }

    @Override
    public List<Subcommand> getSubcommands() {
        return List.of(
            new AlertsSubcommand(),
            new DisableSubcommand(),
            new EnableSubcommand(),
            new HelpSubcommand(),
            new ReloadSubcommand()
        );
    }

    public static void showHelp(final CommandSender sender) {
        sender.sendMessage(deserialize("&7&m                                                            "));
        sender.sendMessage(deserialize("&bSnowReports &e" + SnowReports.VERSION + " &fby &dSnowz"));
        sender.sendMessage(deserialize(""));
        sender.sendMessage(deserialize("&fHover over a subcommand"));
        sender.sendMessage(deserialize("&fto view its description."));
        sender.sendMessage(deserialize(""));

        help.forEach((command, description) -> {
            final Component message = deserialize(formatCommand(command))
                .hoverEvent(HoverEvent.showText(deserialize("&f" + description)));

            sender.sendMessage(message);
        });

        sender.sendMessage(deserialize("&7&m                                                            "));
    }

    private static String formatCommand(final String command) {
        final Pattern pattern = Pattern.compile("\\(.*?\\)|\\S+");
        final Matcher matcher = pattern.matcher(command);

        final String[] parts = matcher.results().map(MatchResult::group).toArray(String[]::new);
        final AtomicInteger index = new AtomicInteger(0);

        final List<String> list = Arrays.asList(parts);

        list.forEach(part -> {
            final int incremented = index.getAndIncrement();

            if (part.startsWith("(") && part.endsWith(")")) {
                parts[incremented] = "&e" + part;
            } else if (incremented == 0) {
                parts[incremented] = "&b" + part;
            } else if (part.contains("/")) {
                parts[incremented] = part.replace("/", "&e/&e");
            } else {
                parts[incremented] = "&f" + part;
            }
        });

        return String.join(" ", parts);
    }
}
