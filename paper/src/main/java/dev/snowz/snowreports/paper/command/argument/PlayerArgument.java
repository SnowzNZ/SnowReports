package dev.snowz.snowreports.paper.command.argument;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerArgument extends CustomArgument<Player, String> {

    public PlayerArgument(final String name) {
        super(
            new StringArgument(name), info -> {
                final Player player = Bukkit.getPlayer(info.input());
                if (player == null) {
                    throw CustomArgument.CustomArgumentException.fromMessageBuilder(
                        new CustomArgument.MessageBuilder("That player is not online or does not exist!")
                    );
                }
                return player;
            }
        );
        this.replaceSuggestions(ArgumentSuggestions.strings(info ->
            Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toArray(String[]::new)
        ));
    }
}
