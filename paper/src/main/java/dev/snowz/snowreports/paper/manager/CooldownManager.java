package dev.snowz.snowreports.paper.manager;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownManager {

    private final Map<UUID, Instant> cooldowns = new ConcurrentHashMap<>();

    public void setCooldown(final UUID key, final Duration duration) {
        final Instant expiration = Instant.now().plus(duration);
        cooldowns.put(key, expiration);
    }

    public boolean hasCooldown(final UUID key) {
        final Instant expiration = cooldowns.get(key);
        return expiration != null && Instant.now().isBefore(expiration);
    }

    public Instant removeCooldown(final UUID key) {
        return cooldowns.remove(key);
    }

    public Duration getRemainingCooldown(final UUID key) {
        final Instant expiration = cooldowns.get(key);
        final Instant now = Instant.now();
        if (expiration != null && now.isBefore(expiration)) {
            return Duration.between(now, expiration);
        } else {
            return Duration.ZERO;
        }
    }
}
