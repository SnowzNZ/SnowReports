package me.snowznz.snowreports.commands;

import me.snowznz.snowreports.SnowReports;
import org.bukkit.command.*;

import java.util.ArrayList;
import java.util.List;

public class SnowReportsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            return false;
        }

        switch (args[0]) {
            case "reload" -> {
                if (sender.hasPermission("snowreports.admin")) {
                    long startTime = System.currentTimeMillis();
                    SnowReports.getInstance().reloadConfig();
                    long endTime = System.currentTimeMillis();
                    long executionTime = endTime - startTime;
                    sender.sendMessage("§aConfig reloaded in " + executionTime + " ms!");
                    return true;
                }
            }
            case "cooldown" -> {
                if (sender.hasPermission("snowreports.admin")) {
                    if (args.length == 2) {
                        try {
                            SnowReports.getInstance().getConfig().set("report-cooldown", Integer.parseInt(args[1]));
                            SnowReports.getInstance().saveConfig();
                            sender.sendMessage("§aCooldown set to " + args[1] + "!");
                        } catch (CommandException e) {
                            sender.sendMessage("§cCooldown must be an integer!");

                        }
                    }
                    if (args.length == 1) {
                        sender.sendMessage("§bCurrent cooldown is " + SnowReports.getInstance().getConfig().getInt("report-cooldown") + " seconds.");
                    }

                    return true;
                }
            }
            case "version" -> {
                String version = SnowReports.getInstance().getDescription().getVersion();
                sender.sendMessage("§eSnowReports is currently on v" + version + ".");

                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("cooldown");
            completions.add("version");
        } else if (args.length == 2) {
            return null;
        }

        return completions;
    }
}
