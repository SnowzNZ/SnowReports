package dev.snowz.snowreports.bukkit.command.impl.snowreports.subcommand;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.SnowReports;
import dev.snowz.snowreports.bukkit.command.subcommand.Subcommand;
import org.bukkit.entity.Player;

import java.util.List;

import static dev.snowz.snowreports.bukkit.manager.MessageManager.getMessage;

public final class AlertsSubcommand implements Subcommand {

    @Override
    public String getName() {
        return "alerts";
    }

    @Override
    public String getDescription() {
        return "Toggle report alerts.";
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new MultiLiteralArgument("action", "enable", "disable")
                .setOptional(true)
        );
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            final Player player = (Player) sender;
            final String action = (String) args.get("action");
            if (action == null) {
                SnowReports.getAlertManager().setAlerts(
                    player.getUniqueId(),
                    !SnowReports.getAlertManager().hasAlertsEnabled(player.getUniqueId())
                );
            } else {
                switch (action) {
                    case "enable":
                        SnowReports.getAlertManager().enableAlerts(player.getUniqueId());
                        break;
                    case "disable":
                        SnowReports.getAlertManager().disableAlerts(player.getUniqueId());
                        break;
                }
            }

            player.sendMessage(getMessage("alerts." + (SnowReports.getAlertManager().hasAlertsEnabled(player.getUniqueId()) ? "enabled" : "disabled")));
        };
    }
}
