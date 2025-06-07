package dev.snowz.snowreports.common.database.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import dev.snowz.snowreports.common.database.entity.User;

import java.sql.SQLException;
import java.util.List;

public final class UserDao extends BaseDaoImpl<User, String> {

    public UserDao(final ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, User.class);
    }

    /**
     * Find user by UUID
     */
    public User findByUuid(final String uuid) throws SQLException {
        return queryForId(uuid);
    }

    /**
     * Find users by name (partial match)
     */
    public List<User> findByNameLike(final String name) throws SQLException {
        return queryBuilder()
            .where()
            .like("name", "%" + name + "%")
            .query();
    }

    /**
     * Check if user exists
     */
    public boolean exists(final String uuid) throws SQLException {
        return queryForId(uuid) != null;
    }
}
