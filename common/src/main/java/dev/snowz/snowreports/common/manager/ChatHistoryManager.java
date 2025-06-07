package dev.snowz.snowreports.common.manager;

import dev.snowz.snowreports.api.model.ChatMessage;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public final class ChatHistoryManager {

    private static final Map<UUID, LinkedBlockingDeque<ChatMessage>> playerChatHistory = new ConcurrentHashMap<>();
    private final int maxChatHistorySize;
    private final int maxChatAgeSeconds;

    public ChatHistoryManager(final int maxChatHistorySize, final int maxChatAgeSeconds) {
        this.maxChatHistorySize = maxChatHistorySize;
        this.maxChatAgeSeconds = maxChatAgeSeconds;
    }

    /**
     * Store a player's chat message in their history
     *
     * @param playerUuid Player's UUID
     * @param message    The chat message to store
     */
    public void storePlayerMessage(final UUID playerUuid, final String message) {
        final LinkedBlockingDeque<ChatMessage> history =
            playerChatHistory.computeIfAbsent(
                playerUuid,
                k -> new LinkedBlockingDeque<>(maxChatHistorySize)
            );

        // Clean up old messages before adding new one
        cleanupOldMessages(history);

        // Add new message
        final ChatMessage chatMessage = new ChatMessage(message, Instant.now().getEpochSecond());

        // If deque is full, remove the oldest message
        if (history.remainingCapacity() == 0) {
            history.pollFirst();
        }

        history.offer(chatMessage);
    }

    /**
     * Clean up old messages from the chat history
     *
     * @param history The chat history to clean up
     */
    private void cleanupOldMessages(final LinkedBlockingDeque<ChatMessage> history) {
        if (history.isEmpty()) return;

        final long currentTime = System.currentTimeMillis();
        final long maxAgeMillis = maxChatAgeSeconds * 1000L;

        while (!history.isEmpty()) {
            final ChatMessage oldest = history.peekFirst();
            if (oldest != null && currentTime - oldest.timestamp() > maxAgeMillis) {
                history.pollFirst();
            } else {
                break;
            }
        }
    }

    /**
     * Get the last messages sent by a player
     *
     * @param playerUuid Player's UUID
     * @return Array of messages, most recent last
     */
    public ChatMessage[] getPlayerChatHistory(final UUID playerUuid) {
        final LinkedBlockingDeque<ChatMessage> history = playerChatHistory.get(playerUuid);
        if (history == null || history.isEmpty()) {
            return new ChatMessage[0];
        }

        final ChatMessage oldest = history.peekFirst();
        if (oldest != null) {
            final long currentTime = System.currentTimeMillis();
            final long maxAgeMillis = maxChatAgeSeconds * 1000L;

            if (currentTime - oldest.timestamp() > maxAgeMillis) {
                cleanupOldMessages(history);
            }
        }

        return history.toArray(new ChatMessage[0]);
    }

    /**
     * Clear chat history for a player
     *
     * @param playerUuid Player's UUID
     */
    public void clearPlayerChatHistory(final UUID playerUuid) {
        playerChatHistory.remove(playerUuid);
    }

    /**
     * Clean up all old messages across all players
     */
    public void cleanupAllOldMessages() {
        playerChatHistory.entrySet().removeIf(entry -> {
            cleanupOldMessages(entry.getValue());
            return entry.getValue().isEmpty();
        });
    }

}
