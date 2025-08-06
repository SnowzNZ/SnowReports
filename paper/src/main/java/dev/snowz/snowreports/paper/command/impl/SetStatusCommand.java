package dev.snowz.snowreports.paper.command.impl;

import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.api.model.ReportStatus;
import dev.snowz.snowreports.common.database.entity.Report;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.command.Command;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

public final class SetStatusCommand implements Command {

    @Override
    public String getName() {
        return "setstatus";
    }

    @Override
    public String getDescription() {
        return "Change the status of a report.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("changestatus");
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new IntegerArgument("id")
                .replaceSuggestions(ArgumentSuggestions.stringsAsync(suggestReportIds())),
            new StringArgument("status")
                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                    Arrays.stream(ReportStatus.values())
                        .map(ReportStatus::name)
                        .map(String::toLowerCase)
                        .toArray(String[]::new)
                )),
            new MultiLiteralArgument("confirm", "confirm")
                .setOptional(true)
        );
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            final int reportId = (int) args.getOrDefault("id", -1);
            final String statusArg = (String) args.getOrDefault("status", "");
            final String confirm = (String) args.getOrDefault("confirm", "");

            final ReportStatus newStatus;
            try {
                newStatus = ReportStatus.valueOf(statusArg.toUpperCase());
            } catch (final IllegalArgumentException e) {
                sender.sendMessage(getMessage("report.invalid_status", statusArg));
                return;
            }

            if (confirm.equals("confirm")) {
                SnowReports.runAsync(() -> {
                    final Report report = SnowReports.getReportManager().getReportById(reportId);
                    if (report == null) {
                        SnowReports.runSync(() -> sender.sendMessage(getMessage("report.not_found", reportId)));
                        return;
                    }

                    if (report.getStatus() == newStatus) {
                        SnowReports.runSync(() -> sender.sendMessage(getMessage(
                            "report.already_status",
                            reportId,
                            newStatus.name().toLowerCase()
                        )));
                        return;
                    }

                    final boolean updated = SnowReports.getReportManager().updateReportStatus(
                        reportId, newStatus,
                        sender instanceof Player ? (Player) sender : null
                    );

                    SnowReports.runSync(() -> {
                        if (updated) {
                            sender.sendMessage(getMessage(
                                "report.updated_status",
                                reportId,
                                newStatus.name().toLowerCase()
                            ));
                        } else {
                            sender.sendMessage(getMessage("report.update_failed", reportId));
                        }
                    });
                });
            } else {
                sender.sendMessage(getMessage("setstatus.confirm", reportId, newStatus.name().toLowerCase()));
                sender.sendMessage(getMessage("click_to_confirm").clickEvent(
                    ClickEvent.runCommand("/setstatus " + reportId + " " + statusArg + " confirm")
                ));
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
