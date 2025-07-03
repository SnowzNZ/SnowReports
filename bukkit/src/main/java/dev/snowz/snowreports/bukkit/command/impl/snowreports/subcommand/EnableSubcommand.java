package dev.snowz.snowreports.bukkit.command.impl.snowreports.subcommand;

import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.command.subcommand.Subcommand;
import dev.snowz.snowreports.common.config.Config;

import static dev.snowz.snowreports.bukkit.manager.MessageManager.getMessage;

public final class EnableSubcommand implements Subcommand {

    @Override
    public String getName() {
        return "enable";
    }

    @Override
    public String getDescription() {
        return "Enable the report system.";
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            Config.get().getReports().setEnabled(true);
            Config.get().save();
            Config.reload();
            sender.sendMessage(getMessage("reports.enabled"));
        };
    }
}
