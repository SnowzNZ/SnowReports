package dev.snowz.snowreports.bukkit.listener;

import dev.snowz.snowreports.bukkit.SnowReports;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class PlayerChatListener implements Listener {

    public PlayerChatListener() {
        SnowReports.getInstance().getServer()
            .getScheduler()
            .runTaskTimerAsynchronously(
                SnowReports.getInstance(),
                () -> SnowReports.runAsync(() -> SnowReports.getChatHistoryManager().cleanupAllOldMessages()),
                600L,
                600L
            );
    }

    @EventHandler
    public void onPlayerChat(final AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final String message = ((TextComponent) event.message()).content();

        SnowReports.runAsync(() -> SnowReports.getChatHistoryManager().storePlayerMessage(
            player.getUniqueId(),
            message
        ));
    }
}
