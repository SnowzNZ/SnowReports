package dev.snowz.snowreports.commands;

import dev.snowz.snowreports.report.Report;
import dev.snowz.snowreports.utils.ReportManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class Commandreports implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§c§l(!) §cYou must specify a player!");
            return true;
        }

        ReportManager reportManager = new ReportManager();
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (!player.hasPlayedBefore()) {
            sender.sendMessage("§c§l(!) §c" + args[0] + " has not joined before.");
            return true;
        }
        List<Report> reports = reportManager.getReports(player.getUniqueId().toString());
        if (reports.isEmpty()) {
            sender.sendMessage("§a" + player.getName() + " has no reports!");
            return true;
        }

        sender.sendMessage(player.getName() + "'s Reports: (" + reports.size() + ")");

        int pageSize = 3; // Set the number of reports per page
        int pageCount = (int) Math.ceil((double) reports.size() / pageSize);

        for (int page = 1; page <= pageCount; page++) {
            sender.sendMessage("Page " + page + "/" + pageCount);
            sender.sendMessage("§m------------------------------");

            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, reports.size());

            for (int i = startIndex; i < endIndex; i++) {
                Report report = reports.get(i);
                OfflinePlayer reporter = Bukkit.getOfflinePlayer(UUID.fromString(report.getReporterUUID()));

                sender.sendMessage("§eReport ID: " + report.getReportID());
                sender.sendMessage("§aReporter: " + reporter.getName());
                sender.sendMessage("§cReason: " + report.getReason());
                sender.sendMessage("§bDate/Time: " + report.getTimeStamp());
                sender.sendMessage("§m------------------------------");
            }
        }
        return true;
    }

}
