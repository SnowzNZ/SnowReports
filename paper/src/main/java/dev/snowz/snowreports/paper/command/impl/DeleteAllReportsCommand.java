package dev.snowz.snowreports.paper.command.impl;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.command.Command;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.List;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

public final class DeleteAllReportsCommand implements Command {

    @Override
    public String getName() {
        return "deleteallreports";
    }

    @Override
    public String getDescription() {
        return "Delete all reports.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("delallreports", "delreports", "clearreports", "clearallreports");
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new MultiLiteralArgument("confirm", "confirm")
                .setOptional(true)
        );
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            final String confirm = (String) args.getOrDefault("confirm", "");

            if (confirm.equals("confirm")) {
                final boolean deleted = SnowReports.getReportManager().deleteAllReports();

                if (deleted) {
                    sender.sendMessage(getMessage("delallreports.success"));
                } else {
                    sender.sendMessage(getMessage("delallreports.failed"));
                }
            } else {
                sender.sendMessage(getMessage("delallreports.confirm"));
                sender.sendMessage(getMessage("click_to_confirm").clickEvent(ClickEvent.runCommand(
                    "/deleteallreports confirm")));
            }
        };
    }
}
