package dev.snowz.snowreports.paper.manager;

import org.bukkit.entity.Player;
import xyz.xenondevs.invui.window.Window;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class GuiHistoryManager {

    private static final Map<UUID, Deque<Window>> playerWindowHistory = new ConcurrentHashMap<>();

    /**
     * Records a window in the player's navigation history.
     *
     * @param player The player
     * @param window The window to record
     */
    public static void pushWindow(final Player player, final Window window) {
        playerWindowHistory.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>())
            .push(window);
    }

    /**
     * Navigates to the previous window in the player's history.
     * If there's no previous window, closes the player's inventory.
     *
     * @param player The player
     */
    public static void previousMenu(final Player player) {
        final Deque<Window> history = playerWindowHistory.get(player.getUniqueId());
        if (history != null && !history.isEmpty()) {
            final Window previousWindow = history.pop();
            previousWindow.open();
        } else {
            player.closeInventory();
        }
    }

    /**
     * Clears navigation history for a player.
     *
     * @param player The player whose history should be cleared
     */
    public static void clearHistory(final Player player) {
        playerWindowHistory.remove(player.getUniqueId());
    }

    /**
     * Records the current window from a set of windows in the player's navigation history.
     * This is a convenience method for when you have a set of windows and want to record the current one.
     *
     * @param player  The player
     * @param windows Set of windows from which to extract the current window
     * @return true if a window was successfully pushed, false otherwise
     */
    public static boolean pushCurrentWindow(final Player player, final Set<Window> windows) {
        if (windows != null && !windows.isEmpty()) {
            final Window currentWindow = windows.iterator().next();
            pushWindow(player, currentWindow);
            return true;
        }
        return false;
    }
}
