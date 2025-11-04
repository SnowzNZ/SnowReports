package dev.snowz.snowreports.paper.command.impl.snowreports;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.command.Command;
import dev.snowz.snowreports.paper.command.impl.snowreports.subcommand.*;
import dev.snowz.snowreports.paper.command.subcommand.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.snowz.snowreports.paper.manager.MessageManager.deserialize;

public final class SnowReportsCommand implements Command {

    @Override
    public String getName() {
        return "snowreports";
    }

    @Override
    public String getDescription() {
        return "SnowReports main command.";
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

    public void showHelp(final CommandSender sender) {
        sender.sendMessage(deserialize("&7&m                                                            "));
        sender.sendMessage(deserialize("&bSnowReports &e" + SnowReports.VERSION + " &fby &dSnowz"));
        sender.sendMessage(deserialize(""));
        sender.sendMessage(deserialize("&fHover over a command to view its description."));
        sender.sendMessage(deserialize(""));

        for (final Subcommand subcommand : getSubcommands()) {
            final StringBuilder commandFormat = new StringBuilder("/snowreports " + subcommand.getName());

            if (!subcommand.getArguments().isEmpty()) {
                for (final Argument<?> arg : subcommand.getArguments()) {
                    if (arg.isOptional()) {
                        commandFormat.append(" [").append(arg.getNodeName()).append("]");
                    } else {
                        commandFormat.append(" <").append(arg.getNodeName()).append(">");
                    }
                }
            }

            final Component message = deserialize(formatCommand(commandFormat.toString()))
                .hoverEvent(HoverEvent.showText(deserialize("&f" + subcommand.getDescription())))
                .clickEvent(ClickEvent.suggestCommand("/snowreports " + subcommand.getName() + " "));

            sender.sendMessage(message);
        }

        for (final Command command : SnowReports.getCommands()) {
            if (command.getName().equals("snowreports")) {
                continue; // Skip this command since we already displayed its subcommands
            }

            final StringBuilder commandFormat = new StringBuilder("/" + command.getName());

            if (!command.getArguments().isEmpty()) {
                for (final Argument<?> arg : command.getArguments()) {
                    if (arg.isOptional()) {
                        commandFormat.append(" [").append(arg.getNodeName()).append("]");
                    } else {
                        commandFormat.append(" <").append(arg.getNodeName()).append(">");
                    }
                }
            }

            final Component message = deserialize(formatCommand(commandFormat.toString()))
                .hoverEvent(HoverEvent.showText(deserialize("&f" + command.getDescription())))
                .clickEvent(ClickEvent.suggestCommand("/" + command.getName() + " "));

            sender.sendMessage(message);
        }

        sender.sendMessage(deserialize("&7&m                                                            "));
    }

    private static String formatCommand(final String command) {
        final Pattern pattern = Pattern.compile("\\[.*?\\]|<.*?>|\\S+");
        final Matcher matcher = pattern.matcher(command);

        final String[] parts = matcher.results().map(MatchResult::group).toArray(String[]::new);
        final AtomicInteger index = new AtomicInteger(0);

        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            final int currentIndex = index.getAndIncrement();

            if (part.startsWith("[") && part.endsWith("]") || part.startsWith("<") && part.endsWith(">")) {
                parts[currentIndex] = "&e" + part;
            } else if (currentIndex == 0) {
                parts[currentIndex] = "&b" + part;
            } else if (part.contains("/")) {
                parts[currentIndex] = part.replace("/", "&e/&e");
            } else {
                parts[currentIndex] = "&f" + part;
            }
        }

        return String.join(" ", parts);
    }
}
