package dev.snowz.snowreports.bukkit.manager;

import dev.snowz.snowreports.bukkit.SnowReports;
import dev.snowz.snowreports.common.database.entity.User;
import org.bukkit.entity.Player;

public final class UserManager {

    /**
     * Creates or updates a user from a Player object
     *
     * @param player The player to create/update a user for
     * @return The created/updated User object, or null if creation failed
     */
    public User getOrCreateUser(final Player player) {
        return getOrCreateUser(player.getUniqueId().toString(), player.getName());
    }

    /**
     * Creates or updates a user from UUID and name
     *
     * @param uuid Player UUID as string
     * @param name Player name
     * @return The created/updated User object, or null if creation failed
     */
    public User getOrCreateUser(final String uuid, final String name) {
        try {
            final User user = new User(uuid, name);
            SnowReports.getUserDao().createOrUpdate(user);
            return user;
        } catch (final Exception e) {
            SnowReports.getInstance().getLogger().warning("Failed to create or update user " + name + " (" + uuid + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates or updates multiple users at once
     *
     * @param players Array of players to create/update users for
     * @return Array of created/updated User objects (some may be null if creation failed)
     */
    public User[] getOrCreateUsers(final Player... players) {
        final User[] users = new User[players.length];

        for (int i = 0; i < players.length; i++) {
            users[i] = getOrCreateUser(players[i]);
        }

        return users;
    }

    /**
     * Gets a user by UUID string
     *
     * @param uuid Player UUID as string
     * @return The User if found, or null if not found
     */
    public User getUserByUuid(final String uuid) {
        try {
            return SnowReports.getUserDao().queryForId(uuid);
        } catch (final Exception e) {
            SnowReports.getInstance().getLogger().warning("Failed to get user with UUID " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates a user's name if it differs from the current one
     *
     * @param uuid Player UUID as string
     * @param name New name for the player
     * @return true if user was found and updated, false otherwise
     */
    public boolean updateUserName(final String uuid, final String name) {
        try {
            final User user = getUserByUuid(uuid);

            if (user == null) {
                return false;
            }

            final String currentName = user.getName();

            if (currentName != null && !currentName.equals(name)) {
                user.setName(name);
                SnowReports.getUserDao().update(user);
                return true;
            }

            return false;
        } catch (final Exception e) {
            SnowReports.getInstance().getLogger().warning("Failed to update player name for UUID " + uuid + " to " + name + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates a user's name if it differs from the current one
     *
     * @param player The player whose name to update
     * @return true if user was found and updated, false otherwise
     */
    public boolean updateUserName(final Player player) {
        return updateUserName(player.getUniqueId().toString(), player.getName());
    }
}
