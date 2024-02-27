package dev.snowz.snowreports.util;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final Map<UUID, Instant> cooldowns = new HashMap<>();

    public void setCooldown(UUID key, Duration duration) {
        Instant expiration = Instant.now().plus(duration);
        cooldowns.put(key, expiration);
    }

    public boolean hasCooldown(UUID key) {
        Instant expiration = cooldowns.get(key);
        return expiration != null && Instant.now().isBefore(expiration);
    }

    public Instant removeCooldown(UUID key) {
        return cooldowns.remove(key);
    }

    public Duration getRemainingCooldown(UUID key) {
        Instant expiration = cooldowns.get(key);
        Instant now = Instant.now();
        if (expiration != null && now.isBefore(expiration)) {
            return Duration.between(now, expiration);
        } else {
            return Duration.ZERO;
        }
    }
}
