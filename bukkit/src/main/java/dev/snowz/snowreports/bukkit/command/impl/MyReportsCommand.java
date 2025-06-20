package dev.snowz.snowreports.bukkit.command.impl;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.SnowReports;
import dev.snowz.snowreports.bukkit.command.Command;
import dev.snowz.snowreports.bukkit.gui.impl.MyReportsGui;
import dev.snowz.snowreports.common.database.entity.Report;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public final class MyReportsCommand implements Command {

    @Override
    public String getName() {
        return "myreports";
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new IntegerArgument("page")
                .setOptional(true)
        );
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage("This command can only be used by players.");
                return;
            }

            final int page = (int) args.getOrDefault("page", 1);

            SnowReports.runAsync(() -> {
                try {
                    final List<Report> allReports = SnowReports.getReportDao().queryForAll();
                    final List<Report> myReports = allReports.stream()
                        .filter(report ->
                            report.getReporter().getName().equalsIgnoreCase(player.getName()))
                        .collect(Collectors.toList());

                    SnowReports.runSync(() ->
                        new MyReportsGui(myReports, page).open(player)
                    );
                } catch (final SQLException e) {
                    player.sendMessage("Failed to fetch your reports");
                    SnowReports.getInstance().getLogger().severe("Database error: " + e.getMessage());
                }
            });
        };
    }
}
