package dev.snowz.snowreports.bukkit.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    public static String formatEpochTime(final long epochTime, final String pattern) {
        return DateTimeFormatter.ofPattern(pattern)
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochSecond(epochTime));
    }
}
