package dev.snowz.snowreports.bukkit.command.impl.snowreports.subcommand;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.command.subcommand.Subcommand;
import dev.snowz.snowreports.bukkit.manager.MessageManager;
import dev.snowz.snowreports.common.config.Config;

import java.util.List;

import static dev.snowz.snowreports.bukkit.manager.MessageManager.getMessage;

public final class ReloadSubcommand implements Subcommand {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the plugin configuration and messages.";
    }

    @Override
    public List<Argument<?>> getArguments() {
        return List.of(
            new MultiLiteralArgument("file", "config", "messages")
                .setOptional(true)
        );
    }

    @Override
    public CommandExecutor getExecutor() {
        return (sender, args) -> {
            final String file = (String) args.get("file");
            if (file == null) {
                // If no file is specified, reload both config and messages
                Config.reload();
                MessageManager.reload();
                sender.sendMessage(getMessage("config.reloaded"));
                sender.sendMessage(getMessage("messages.reloaded"));
                return;
            }

            switch (file) {
                case "config":
                    Config.reload();
                    sender.sendMessage(getMessage("config.reloaded"));
                    break;
                case "messages":
                    MessageManager.reload();
                    sender.sendMessage(getMessage("messages.reloaded"));
                    break;
            }
        };
    }
}
