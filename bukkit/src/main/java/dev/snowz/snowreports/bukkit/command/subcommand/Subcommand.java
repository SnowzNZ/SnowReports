package dev.snowz.snowreports.bukkit.command.subcommand;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * SnowReports Subcommand
 *
 * @author Snowz
 * @since 1.0.0
 */
public interface Subcommand {

    /**
     * Gets the name of the subcommand.
     *
     * @return The subcommand name
     */
    String getName();

    /**
     * Gets the description of the subcommand.
     * This is used for help messages and command descriptions.
     *
     * @return The subcommand description
     */
    String getDescription();

    /**
     * Gets the permission required to use this subcommand.
     * By default, uses the format "snowreports.command.parent.name".
     *
     * @param parentCommandName The name of the parent command
     * @return The permission string
     */
    default String getPermission(final String parentCommandName) {
        return "snowreports.command." + parentCommandName + "." + getName();
    }

    /**
     * Gets the arguments for this subcommand.
     *
     * @return The list of arguments for this subcommand
     */
    default List<Argument<?>> getArguments() {
        return new ArrayList<>();
    }

    /**
     * Gets the getExecutor for this subcommand.
     *
     * @return The getExecutor for this subcommand
     */
    CommandExecutor getExecutor();
}
