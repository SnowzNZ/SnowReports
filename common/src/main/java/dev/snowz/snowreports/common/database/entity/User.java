package dev.snowz.snowreports.common.database.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.snowz.snowreports.api.model.UserModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@DatabaseTable(tableName = "users")
public final class User {
    @DatabaseField(
        id = true,
        columnName = "uuid"
    )
    private String uuid;

    @DatabaseField(
        columnName = "name",
        canBeNull = false
    )
    private String name;

    public User() {
    }

    public User(final String uuid, final String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UserModel toModel() {
        return new UserModel(uuid, name);
    }

    public static User fromModel(final UserModel model) {
        final User user = new User();
        user.setUuid(model.uuid());
        user.setName(model.name());
        return user;
    }

    @Override
    public String toString() {
        return "User{uuid='" + uuid + "', name='" + name + "'}";
    }
}
