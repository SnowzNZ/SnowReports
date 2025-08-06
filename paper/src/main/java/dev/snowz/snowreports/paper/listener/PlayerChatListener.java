package dev.snowz.snowreports.paper.listener;

import dev.snowz.snowreports.paper.SnowReports;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PlayerChatListener implements Listener {

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SnowReports-Chat-" + THREAD_COUNTER.incrementAndGet());
        t.setDaemon(true);
        return t;
    });

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(final AsyncChatEvent event) {
        final Player player = event.getPlayer();
        final String message = ((TextComponent) event.message()).content();

        EXECUTOR.submit(() -> {
            try {
                SnowReports.getChatHistoryManager().storePlayerMessage(
                    player.getUniqueId(),
                    message
                );
            } catch (final Exception e) {
                SnowReports.getInstance().getLogger().warning(
                    "Failed to store chat message for player " + player.getName() + ": " + e.getMessage()
                );
            }
        });
    }

    public static void shutdown() {
        EXECUTOR.shutdown();

        try {
            if (!EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (final InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
