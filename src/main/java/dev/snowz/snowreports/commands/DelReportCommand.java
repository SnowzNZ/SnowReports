package dev.snowz.snowreports.commands;

import dev.snowz.snowreports.SnowReports;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class DelReportCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§c§l(!) §cYou must specify a Report ID!");
            return true;
        }

        try {
            int reportID = Integer.parseInt(args[0]);
            boolean removed = SnowReports.getDb().deleteReport(reportID);

            if (removed) {
                sender.sendMessage("§aReport #" + reportID + " deleted!");
            } else {
                sender.sendMessage("§c§l(!) §cReport #" + reportID + " not found!");
            }
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage("§c§l(!) §cReport ID must be a integer!");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SnowReports.getDb().getAllReportIDs();
        }
        return null;
    }
}
