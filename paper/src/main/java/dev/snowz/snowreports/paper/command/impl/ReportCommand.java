package dev.snowz.snowreports.paper.command.impl;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.command.Command;
import dev.snowz.snowreports.paper.command.argument.PlayerArgument;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

public final class ReportCommand implements Command {

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public String getDescription() {
        return "Report a player.";
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new PlayerArgument("player"),
            new GreedyStringArgument("reason")
                .replaceSuggestions(ArgumentSuggestions.strings(Config.get().getReports().getReason().getPresets()))
                .setOptional(!Config.get().getReports().getReason().isRequired())
        );
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            if (!(sender instanceof final Player reporter)) {
                sender.sendMessage(getMessage("error.player_only"));
                return;
            }

            final Player reportedPlayer = (Player) args.get("player");
            final String reason = (String) args.getOrDefault("reason", "");

            if (!Config.get().getReports().isEnabled()) {
                reporter.sendMessage(getMessage("error.reports_disabled"));
                return;
            }

            if (!Config.get().getReports().getReason().isCustomAllowed()) {
                if (!Arrays.stream(Config.get().getReports().getReason().getPresets()).toList().contains(reason)) {
                    reporter.sendMessage(getMessage("error.preset_only"));
                    return;
                }
            }

            final int charLimit = Config.get().getReports().getReason().getCharacterLimit();
            if (charLimit != -1 && reason.length() > charLimit) {
                reporter.sendMessage(getMessage(
                    "error.reason_too_long",
                    charLimit
                ));
                return;
            }

            if (reportedPlayer == null) {
                reporter.sendMessage(getMessage("error.player_not_found"));
                return;
            }

            if (reportedPlayer.equals(reporter) && !Config.get().getDebug().isAllowSelfReport()) {
                reporter.sendMessage(getMessage("error.cannot_self_report"));
                return;
            }

            if (reportedPlayer.hasPermission("snowreports.bypass.report")) {
                reporter.sendMessage(getMessage("report.cannot_report"));
                return;
            }

            final Duration timeLeft = SnowReports.getCooldownManager().getRemainingCooldown(reporter.getUniqueId());

            if (timeLeft.isZero() || timeLeft.isNegative()) {
                SnowReports.getReportManager().processReport(reporter, reportedPlayer, reason);
            } else {
                reporter.sendMessage(getMessage("error.cooldown", timeLeft.toMinutes()));
            }
        };
    }
}
