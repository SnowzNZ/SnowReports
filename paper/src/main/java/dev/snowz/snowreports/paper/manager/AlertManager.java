package dev.snowz.snowreports.paper.manager;

import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.common.database.entity.Report;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

public final class AlertManager {
    private final Set<UUID> alertDisabled = ConcurrentHashMap.newKeySet();

    /**
     * Enable alerts for a player
     *
     * @param uuid The UUID of the player
     */
    public void enableAlerts(final UUID uuid) {
        alertDisabled.remove(uuid);
    }

    /**
     * Disable alerts for a player
     *
     * @param uuid The UUID of the player
     */
    public void disableAlerts(final UUID uuid) {
        alertDisabled.add(uuid);
    }

    /**
     * Set alerts for a player
     *
     * @param uuid    The UUID of the player
     * @param enabled true to enable alerts, false to disable
     */
    public void setAlerts(final UUID uuid, final boolean enabled) {
        if (enabled) {
            enableAlerts(uuid);
        } else {
            disableAlerts(uuid);
        }
    }

    /**
     * Check if a player has alerts enabled
     *
     * @param uuid The UUID of the player
     * @return true if the player has alerts enabled (not in disabled set)
     */
    public boolean hasAlertsEnabled(final UUID uuid) {
        return !alertDisabled.contains(uuid);
    }

    /**
     * Get all players who have alerts disabled
     *
     * @return An unmodifiable set of UUIDs of players with alerts disabled
     */
    public Set<UUID> getAlertDisabled() {
        return Collections.unmodifiableSet(alertDisabled);
    }

    public void broadcastAlert(final Report report) {
        final Component message = getMessage(
            "report.notify",
            report.getReported().getName(),
            report.getReporter().getName(),
            report.getReason()
        );

        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("snowreports.alerts") && !alertDisabled.contains(player.getUniqueId())) {
                player.sendMessage(message);
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            }
        }

        if (Config.get().getReports().isNotifyConsole()) {
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }
}
