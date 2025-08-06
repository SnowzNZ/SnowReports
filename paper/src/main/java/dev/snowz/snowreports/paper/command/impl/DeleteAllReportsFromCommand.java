package dev.snowz.snowreports.paper.command.impl;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.common.database.entity.User;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.command.Command;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.List;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

public final class DeleteAllReportsFromCommand implements Command {

    @Override
    public String getName() {
        return "deleteallreportsfrom";
    }

    @Override
    public String getDescription() {
        return "Delete all reports made by a player.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("delallreportsfrom", "delreportsfrom", "clearreportsfrom", "clearallreportsfrom");
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new StringArgument("playerName"),
            new MultiLiteralArgument("confirm", "confirm")
                .setOptional(true)
        );
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            final String playerName = (String) args.get("playerName");
            final String confirm = (String) args.getOrDefault("confirm", "");

            final User user = SnowReports.getUserManager().getUserByName(playerName);
            assert user != null;

            if (confirm.equals("confirm")) {
                final boolean deleted = SnowReports.getReportManager().deleteAllReportsFrom(user);

                if (deleted) {
                    sender.sendMessage(getMessage("delallreportsfrom.success"));
                } else {
                    sender.sendMessage(getMessage("delallreportsfrom.failed"));
                }
            } else {
                sender.sendMessage(getMessage("delallreportsfrom.confirm"));
                sender.sendMessage(getMessage("click_to_confirm").clickEvent(ClickEvent.runCommand(
                    "/deleteallreportsfrom " + playerName + " confirm")));
            }
        };
    }
}
