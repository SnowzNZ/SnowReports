package dev.snowz.snowreports.bukkit.command.impl;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.SnowReports;
import dev.snowz.snowreports.bukkit.command.Command;
import dev.snowz.snowreports.bukkit.gui.impl.ReportsGui;
import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.common.database.entity.Report;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static dev.snowz.snowreports.bukkit.manager.MessageManager.getMessage;
import static dev.snowz.snowreports.bukkit.util.TimeUtil.formatEpochTime;

public final class ReportsCommand implements Command {

    @Override
    public String getName() {
        return "reports";
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new StringArgument("playerOrPage")
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
            int page = 1;

            final String arg = (String) args.get("playerOrPage");
            if (arg != null) {
                try {
                    page = Integer.parseInt(arg);
                } catch (final NumberFormatException e) {
                    playerName = arg;
                }
            }

            try {
                if (sender instanceof final ConsoleCommandSender console) {
                    displayReportsToConsole(console, playerName, page);
                } else if (sender instanceof final Player player) {
                    displayReportsToPlayer(player, playerName, page);
                }
            } catch (final SQLException e) {
                sender.sendMessage("An error occurred while fetching reports.");
                SnowReports.getInstance().getLogger().severe("Error fetching reports: " + e.getMessage());
            }
        };
    }

    private void displayReportsToConsole(final ConsoleCommandSender sender, final String playerName, final int page) {
        SnowReports.runAsync(() -> {
            try {
                final List<Report> allReports = SnowReports.getReportDao().queryForAll();
                final List<Report> filteredReports = filterReportsByPlayer(allReports, playerName);

                SnowReports.runSync(
                    () -> sendReportsToConsole(sender, filteredReports, page, playerName)
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
        final int page
    ) throws SQLException {
        SnowReports.runAsync(() -> {
            try {
                final List<Report> allReports = SnowReports.getReportDao().queryForAll();
                final List<Report> filteredReports = filterReportsByPlayer(allReports, playerName);

                final String title = playerName == null ?
                    "Reports" : "Reports for " + playerName;

                SnowReports.runSync(() -> new ReportsGui(filteredReports, page, title).open(player));
            } catch (final SQLException e) {
                player.sendMessage("Failed to fetch reports");
                SnowReports.getInstance().getLogger().severe("Database error: " + e.getMessage());
            }
        });
    }

    private List<Report> filterReportsByPlayer(final List<Report> reports, final String playerName) {
        if (playerName == null) {
            return reports;
        }

        return reports.stream()
            .filter(report ->
                report.getReported().getName().equalsIgnoreCase(playerName) ||
                    report.getReporter().getName().equalsIgnoreCase(playerName))
            .collect(Collectors.toList());
    }

    private void sendReportsToConsole(
        final ConsoleCommandSender sender,
        final List<Report> reports,
        final int page,
        final String playerName
    ) {
        final int pageSize = 5;
        final int pageCount = Math.max(1, (int) Math.ceil((double) reports.size() / pageSize));

        if (page < 1 || page > pageCount) {
            sender.sendMessage(getMessage("error.invalid_page_number", pageCount));
            return;
        }

        final String header = playerName == null ?
            "All Reports" : "Reports for " + playerName;

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
            sender.sendMessage("Updated by: " + report.getUpdatedBy().getName());
            sender.sendMessage("Server: " + report.getServer());
            sender.sendMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }
    }
}

