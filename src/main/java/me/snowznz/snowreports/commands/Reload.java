package me.snowznz.snowreports.commands;

import me.snowznz.snowreports.SnowReports;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reload implements CommandExecutor, TabCompleter {
    private static final String[] commands = { "reload", "cooldown"};

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 1) {
            return false;
        }

        String option = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (args[0].equals("reload")) {
            if (sender.hasPermission("snowreports.admin")) {
                SnowReports.getInstance().reloadConfig();
                sender.sendMessage("§aConfig reloaded!");
                return true;
            }
        }
        if (args[0].equals("cooldown")) {
            if (sender.hasPermission("snowreports.admin")) {
                SnowReports.getInstance().getConfig().set("report-cooldown", Integer.parseInt(option));
                SnowReports.getInstance().saveConfig();
                sender.sendMessage("§aCooldown set to " + option + "!");
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], Arrays.asList(commands), completions);
        return completions;
    }
}
