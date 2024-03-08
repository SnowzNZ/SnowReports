package dev.snowz.snowreports.commands;

import dev.snowz.snowreports.L10n;
import dev.snowz.snowreports.SnowReports;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SnowReportsCommand implements CommandExecutor, TabCompleter {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            SnowReports.getInstance().getConfig().set("reports.enabled", true);
            SnowReports.getInstance().saveConfig();
            SnowReports.getInstance().reloadConfig();
            sender.sendMessage("§aEnabled reports!");
            return true;
        } else if (args[0].equalsIgnoreCase("disable")) {
            SnowReports.getInstance().getConfig().set("reports.enabled", false);
            SnowReports.getInstance().saveConfig();
            SnowReports.getInstance().reloadConfig();
            sender.sendMessage("§cDisabled reports!");
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            SnowReports.getInstance().reloadConfig();
            sender.sendMessage("§aReloaded config!");
            return true;
        } else if (args[0].equalsIgnoreCase("test")) {
            sender.sendMessage(L10n.getPlayerLocale((Player) sender));
            sender.sendMessage(L10n.getMessage("test", L10n.getPlayerLocale((Player) sender), sender.getName()));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("enable", "disable", "reload");
        }
        return null;
    }
}
