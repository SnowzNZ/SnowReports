package dev.snowz.snowreports.bukkit.command.impl.snowreports.subcommand;

import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.command.subcommand.Subcommand;
import dev.snowz.snowreports.common.config.Config;

import static dev.snowz.snowreports.bukkit.manager.MessageManager.getMessage;

public final class DisableSubcommand implements Subcommand {

    @Override
    public String getName() {
        return "disable";
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            Config.get().getReports().setEnabled(false);
            Config.get().save();
            Config.reload();
            sender.sendMessage(getMessage("reports.disabled"));
        };
    }
}
