package dev.snowz.snowreports.commands;

import dev.snowz.snowreports.utils.ReportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commanddelreport implements CommandExecutor {

    ReportManager reportManager = new ReportManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§c§l(!) §cYou must specify a Report ID!");
            return true;
        }
        try {
            int reportID = Integer.parseInt(args[0]);
            if (reportManager.doesReportIDExist(reportID)) {
                reportManager.deleteReport(reportID);
                sender.sendMessage("§aReport #" + reportID + " deleted!");
            } else {
                sender.sendMessage("§c§l(!) §c" + reportID + " is not a valid Report ID!");
            }
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage("§c§l(!) §cReport ID must be a integer!");
            return true;
        }
    }
}
