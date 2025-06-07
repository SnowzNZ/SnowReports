package dev.snowz.snowreports.bukkit.command.subcommand;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.executors.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * SnowReports Subcommand
 *
 * @author Snowz
 * @since 2.0.0
 */
public interface Subcommand {

    /**
     * Gets the name of the subcommand.
     *
     * @return The subcommand name
     */
    String getName();

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
