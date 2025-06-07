package dev.snowz.snowreports.api.model;

/**
 * Represents a chat message with its content and timestamp
 *
 * @param message   the content of the message
 * @param timestamp the time when the message was sent, in milliseconds since epoch
 */
public record ChatMessage(String message, long timestamp) {
}
