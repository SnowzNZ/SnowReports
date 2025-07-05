package dev.snowz.snowreports.bukkit.command.impl.snowreports.subcommand;

import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.command.impl.snowreports.SnowReportsCommand;
import dev.snowz.snowreports.bukkit.command.subcommand.Subcommand;

public final class HelpSubcommand implements Subcommand {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show help for SnowReports commands.";
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> new SnowReportsCommand().showHelp(sender);
    }
}
