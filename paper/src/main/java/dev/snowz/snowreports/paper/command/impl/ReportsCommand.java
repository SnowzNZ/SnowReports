package dev.snowz.snowreports.paper.command.impl;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.common.database.entity.Report;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.command.Command;
import dev.snowz.snowreports.paper.gui.impl.ReportsGui;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;
import static dev.snowz.snowreports.paper.util.TimeUtil.formatEpochTime;

public final class ReportsCommand implements Command {

    @Override
    public String getName() {
        return "reports";
    }

    @Override
    public String getDescription() {
        return "View all reports or reports for a specific player.";
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new TextArgument("playerOrPage")
                .replaceSuggestions(ArgumentSuggestions.strings(
                    Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()))
                )
                .setOptional(true),
            new IntegerArgument("page")
                .setOptional(true)
        );
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            String playerName = null;
            String serverName = null;
            int page = 1;

            final String arg = (String) args.get("playerOrPage");
            if (arg != null) {
                if (arg.startsWith("s:")) {
                    serverName = arg.substring(2);
                } else {
                    try {
                        page = Integer.parseInt(arg);
                    } catch (final NumberFormatException e) {
                        playerName = arg;
                    }
                }
            }

            if (sender instanceof final ConsoleCommandSender console) {
                displayReportsToConsole(console, playerName, serverName, page);
            } else if (sender instanceof final Player player) {
                displayReportsToPlayer(player, playerName, serverName, page);
            }
        };
    }

    private void displayReportsToConsole(
        final ConsoleCommandSender sender,
        final String playerName,
        final String serverName,
        final int page
    ) {
        SnowReports.runAsync(() -> {
            try {
                final List<Report> allReports = SnowReports.getReportDao().queryForAll();
                final List<Report> filteredReports = filterReports(allReports, playerName, serverName);

                SnowReports.runSync(
                    () -> sendReportsToConsole(sender, filteredReports, page, playerName, serverName)
                );
            } catch (final SQLException e) {
                sender.sendMessage("Failed to fetch reports: " + e.getMessage());
                SnowReports.getInstance().getLogger().severe("Database error: " + e.getMessage());
            }
        });
    }

    private void displayReportsToPlayer(
        final Player player,
        final String playerName,
        final String serverName,
        final int page
    ) {
        SnowReports.runAsync(() -> {
            try {
                final List<Report> allReports = SnowReports.getReportDao().queryForAll();
                final List<Report> filteredReports = filterReports(allReports, playerName, serverName);

                final String title = buildTitle(playerName, serverName);

                SnowReports.runSync(() -> {
                    if (filteredReports.isEmpty()) {
                        player.sendMessage(getMessage("reports.no_reports"));
                        return;
                    }

                    new ReportsGui(filteredReports, page, title).open(player);
                });
            } catch (final SQLException e) {
                player.sendMessage("Failed to fetch reports");
                SnowReports.getInstance().getLogger().severe("Database error: " + e.getMessage());
            }
        });
    }

    private List<Report> filterReports(final List<Report> reports, final String playerName, final String serverName) {
        return reports.stream()
            .filter(report -> {
                final boolean matchesPlayer = playerName == null ||
                    report.getReported().getName().equalsIgnoreCase(playerName) ||
                    report.getReporter().getName().equalsIgnoreCase(playerName);

                final boolean matchesServer = serverName == null ||
                    report.getServer().equalsIgnoreCase(serverName);

                return matchesPlayer && matchesServer;
            })
            .collect(Collectors.toList());
    }

    private String buildTitle(final String playerName, final String serverName) {
        if (playerName != null && serverName != null) {
            return "Reports for " + playerName + " on " + serverName;
        } else if (playerName != null) {
            return "Reports for " + playerName;
        } else if (serverName != null) {
            return "Reports on " + serverName;
        } else {
            return "Reports";
        }
    }

    private void sendReportsToConsole(
        final ConsoleCommandSender sender,
        final List<Report> reports,
        final int page,
        final String playerName,
        final String serverName
    ) {
        final int pageSize = 5;
        final int pageCount = Math.max(1, (int) Math.ceil((double) reports.size() / pageSize));

        if (page < 1 || page > pageCount) {
            sender.sendMessage(getMessage("error.invalid_page_number", pageCount));
            return;
        }

        final String header = buildTitle(playerName, serverName);

        sender.sendMessage(header + " (Page " + page + "/" + pageCount + ")");
        sender.sendMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (reports.isEmpty()) {
            sender.sendMessage("No reports found.");
            return;
        }

        final int startIndex = (page - 1) * pageSize;
        final int endIndex = Math.min(startIndex + pageSize, reports.size());

        for (int i = startIndex; i < endIndex; i++) {
            final Report report = reports.get(i);
            sender.sendMessage("ID: " + report.getId());
            sender.sendMessage("Reported: " + report.getReported().getName());
            sender.sendMessage("Reporter: " + report.getReporter().getName());
            sender.sendMessage("Reason: " + report.getReason());
            sender.sendMessage("Created at: " + formatEpochTime(
                report.getCreatedAt(),
                Config.get().getTimeFormat()
            ));
            sender.sendMessage("Status: " + report.getStatus().name());
            sender.sendMessage("Updated at: " + formatEpochTime(
                report.getLastUpdated(),
                Config.get().getTimeFormat()
            ));
            sender.sendMessage("Updated by: " + (report.getUpdatedBy() != null ? report.getUpdatedBy().getName() : "N/A"));
            sender.sendMessage("Server: " + report.getServer());
            sender.sendMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
    }
}
