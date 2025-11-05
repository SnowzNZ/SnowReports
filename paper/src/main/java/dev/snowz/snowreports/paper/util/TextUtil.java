package dev.snowz.snowreports.paper.util;

import java.util.ArrayList;
import java.util.List;

public final class TextUtil {

    /**
     * Wraps text to multiple lines if it exceeds the specified width
     *
     * @param text     The text to wrap
     * @param prefix   The prefix to add to each line (e.g., "§7• §fReason: ")
     * @param continuationPrefix The prefix for continuation lines (e.g., "§7  ")
     * @param maxWidth The maximum width before wrapping
     * @return List of wrapped lines with the prefix
     */
    public static List<String> wrapText(final String text, final String prefix, final String continuationPrefix, final int maxWidth) {
        final List<String> lines = new ArrayList<>();
        if (text.length() <= maxWidth) {
            lines.add(prefix + text);
            return lines;
        }

        final String[] words = text.split(" ");
        final StringBuilder currentLine = new StringBuilder();

        for (final String word : words) {
            if (currentLine.length() + word.length() + 1 > maxWidth) {
                if (!currentLine.isEmpty()) {
                    lines.add((lines.isEmpty() ? prefix : continuationPrefix) + currentLine.toString());
                    currentLine.setLength(0);
                }
            }
            if (!currentLine.isEmpty()) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }

        if (!currentLine.isEmpty()) {
            lines.add((lines.isEmpty() ? prefix : continuationPrefix) + currentLine.toString());
        }

        return lines;
    }
}
