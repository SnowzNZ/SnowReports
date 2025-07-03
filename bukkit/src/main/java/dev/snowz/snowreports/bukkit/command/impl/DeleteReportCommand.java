package dev.snowz.snowreports.bukkit.command.impl;

import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.SnowReports;
import dev.snowz.snowreports.bukkit.command.Command;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static dev.snowz.snowreports.bukkit.manager.MessageManager.getMessage;

public final class DeleteReportCommand implements Command {

    @Override
    public String getName() {
        return "deletereport";
    }

    @Override
    public String getDescription() {
        return "Delete a report.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("delreport");
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new IntegerArgument("id")
                .replaceSuggestions(ArgumentSuggestions.stringsAsync(suggestReportIds())),
            new MultiLiteralArgument("confirm", "confirm")
                .setOptional(true)
        );
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            final int reportId = (int) args.getOrDefault("id", -1);
            final String confirm = (String) args.getOrDefault("confirm", "");

            if (confirm.equals("confirm")) {
                final boolean deleted = SnowReports.getReportManager().deleteReport(reportId);

                if (deleted) {
                    sender.sendMessage(getMessage("report.deleted", reportId));
                } else {
                    sender.sendMessage(getMessage("report.not_found", reportId));
                }

            } else {
                sender.sendMessage(getMessage("delreport.confirm", reportId));
                sender.sendMessage(getMessage("click_to_confirm").clickEvent(ClickEvent.runCommand("/delreport " + reportId + " confirm")));
            }
        };
    }

    private Function<SuggestionInfo<CommandSender>, CompletableFuture<String[]>> suggestReportIds() {
        return info -> CompletableFuture.supplyAsync(() ->
            SnowReports.getReportManager().getReportIds()
                .stream()
                .map(String::valueOf)
                .toArray(String[]::new)
        );
    }
}
