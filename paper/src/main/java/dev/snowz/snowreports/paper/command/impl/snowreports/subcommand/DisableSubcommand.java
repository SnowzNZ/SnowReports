package dev.snowz.snowreports.paper.command.impl.snowreports.subcommand;

import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.paper.command.subcommand.Subcommand;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

public final class DisableSubcommand implements Subcommand {

    @Override
    public String getName() {
        return "disable";
    }

    @Override
    public String getDescription() {
        return "Disable the report system.";
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
