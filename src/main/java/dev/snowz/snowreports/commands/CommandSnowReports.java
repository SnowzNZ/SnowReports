package dev.snowz.snowreports.commands;

import dev.snowz.snowreports.SnowReports;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class CommandSnowReports implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        SnowReports.getInstance().reloadConfig();
        sender.sendMessage("§aReloaded config!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return null;
    }
}
