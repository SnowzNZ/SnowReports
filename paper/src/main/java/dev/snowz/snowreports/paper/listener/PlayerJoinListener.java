package dev.snowz.snowreports.paper.listener;

import dev.snowz.snowreports.paper.SnowReports;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

public final class PlayerJoinListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void updateUserName(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        SnowReports.runAsync(() -> SnowReports.getUserManager().updateUserName(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void updateNotifier(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (player.isOp() || player.hasPermission("snowreports.update")) {
            if (SnowReports.isUpdateAvailable()) {
                player.sendMessage(getMessage("update_available", SnowReports.VERSION));
            }
        }
    }
}
