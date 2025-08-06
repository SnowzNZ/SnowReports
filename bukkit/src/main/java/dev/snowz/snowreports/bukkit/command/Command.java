package dev.snowz.snowreports.bukkit.command;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.snowz.snowreports.bukkit.command.subcommand.Subcommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SnowReports Command
 *
 * @author Snowz
 * @since 1.0.0
 */
public interface Command {

    /**
     * Gets the name of the command.
     * This is used as the primary command identifier (e.g., "report" for /report).
     *
     * @return The command name
     */
    String getName();

    /**
     * Gets the description of the command.
     * This is used for help messages and command descriptions.
     *
     * @return The command description
     */
    String getDescription();

    /**
     * Gets alternative names for this command.
     * These are registered as command aliases.
     *
     * @return List of command aliases
     */
    default List<String> getAliases() {
        return Collections.emptyList();
    }

    /**
     * Gets the permission required to use this command.
     * By default, uses the format "snowreports.command.[name]".
     *
     * @return The permission string
     */
    default String getPermission() {
        return "snowreports.command." + getName();
    }

    /**
     * Gets the arguments for the main command.
     * These are used when the command is executed without subcommands.
     *
     * @return List of command arguments
     */
    default List<Argument<?>> getArguments() {
        return new ArrayList<>();
    }

    /**
     * Gets the subcommands for this command.
     * Each subcommand will be registered as a separate branch.
     *
     * @return List of subcommands
     */
    default List<Subcommand> getSubcommands() {
        return Collections.emptyList();
    }

    /**
     * Gets the executor for the main command.
     * This is triggered when the command is executed without subcommands.
     *
     * @return The command executor
     */
    default CommandExecutor getExecutor() {
        return (sender, args) -> {
        };
    }

    /**
     * Registers this command with the CommandAPI.
     * This handles unregistering existing commands, setting up the command tree,
     * and registering both the main command and any subcommands.
     */
    default void register() {
        // Unregister commands with the same name
        CommandAPIBukkit.unregister(getName(), true, false);

        // Unregister commands with the same name as an alias
        getAliases().forEach(alias -> CommandAPIBukkit.unregister(alias, true, false));

        // Create the command tree
        CommandAPICommand command = new CommandAPICommand(getName())
            .withAliases(getAliases().toArray(new String[0]))
            .withPermission(getPermission())
            .executes(getExecutor());

        if (!getArguments().isEmpty()) {
            command.setArguments(getArguments());
        }

        for (final Subcommand subcommand : getSubcommands()) {
            command = command.withSubcommand(
                new CommandAPICommand(subcommand.getName())
                    .withArguments(subcommand.getArguments())
                    .withPermission(subcommand.getPermission(getName()))
                    .executes(subcommand.getExecutor())
            );
        }

        // Register the command tree
        command.register();
    }
}
