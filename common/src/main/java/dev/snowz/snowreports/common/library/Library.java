package dev.snowz.snowreports.common.library;

import lombok.Getter;

@Getter
public enum Library {
    MYSQL("com.mysql", "mysql-connector-j", "9.3.0"),
    MARIADB("org.mariadb.jdbc", "mariadb-java-client", "3.5.3"),
    POSTGRESQL("org.postgresql", "postgresql", "42.7.5"),
    H2("com.h2database", "h2", "2.3.232"),
    SQLITE("org.xerial", "sqlite-jdbc", "3.49.1.0");

    private final String group;
    private final String name;
    private final String version;

    Library(final String group, final String name, final String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    public String getMavenDependency() {
        return String.format("%s:%s:%s", group, name, version);
    }
}
