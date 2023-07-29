package me.snowznz.snowreports.commands;

import me.snowznz.snowreports.SnowReports;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SnowReportsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("snowreports.admin")) {
                SnowReports.getInstance().reloadConfig();
                sender.sendMessage("§aConfig reloaded!");
            }
        } else if (args[0].equalsIgnoreCase("cooldown")) {
            if (sender.hasPermission("snowreports.admin")) {
                if (args.length == 2) {
                    try {
                        SnowReports.getInstance().getConfig().set("report-cooldown", Integer.parseInt(args[1]));
                        SnowReports.getInstance().saveConfig();
                        sender.sendMessage("§aCooldown set to " + args[1] + "!");
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cCooldown must be an integer!");
                    }
                } else if (args.length == 1) {
                    sender.sendMessage("§bCurrent report cooldown is " + SnowReports.getInstance().getConfig().getInt("report-cooldown") + " seconds.");
                }
            }
        } else if (args[0].equalsIgnoreCase("version")) {
            String version = SnowReports.getInstance().getDescription().getVersion();
            sender.sendMessage("§eSnowReports is currently on v" + version + ".");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Stream.of("reload", "cooldown", "version")
                    .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
