package dev.snowz.snowreports.paper.command.impl;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.common.database.entity.Report;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.command.Command;
import dev.snowz.snowreports.paper.gui.impl.MyReportsGui;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

public final class MyReportsCommand implements Command {

    @Override
    public String getName() {
        return "myreports";
    }

    @Override
    public String getDescription() {
        return "View your submitted reports.";
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
                    final List<Report> myReports = SnowReports.getReportDao().queryBuilder()
                        .where()
                        .eq("reporter_uuid", player.getUniqueId().toString())
                        .query();

                    SnowReports.runSync(() -> {
                            if (myReports.isEmpty()) {
                                player.sendMessage(getMessage("myreports.no_reports"));
                                return;
                            }
                            new MyReportsGui(myReports, page).open(player);
                        }
                    );
                } catch (final SQLException e) {
                    player.sendMessage("Failed to fetch your reports");
                    SnowReports.getInstance().getLogger().severe("Database error: " + e.getMessage());
                }
            });
        };
    }
}
