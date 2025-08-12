package dev.snowz.snowreports.common.config;

import de.exlll.configlib.*;
import dev.snowz.snowreports.common.database.type.DatabaseType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Configuration
@NoArgsConstructor
public final class Config {
    private static Config instance;
    private static File dataFolder;

    private static final String CONFIG_HEADER = """
        \s
         ,---.               ,---.                    |        \s
         `---.,---.,---.. . .|---',---.,---.,---.,---.|--- ,---.
             ||   ||   || | ||  \\ |---'|   ||   ||    |    `---.
         `---'`   '`---'`-'-'`   ``---'|---'`---'`    `---'`---'
                                       |                        \s
        \s""";

    private static final YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder()
        .charset(StandardCharsets.UTF_8)
        .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
        .addSerializer(DatabaseType.class, new DatabaseTypeSerializer())
        .header(CONFIG_HEADER)
        .build();

    private String serverName = "server";

    private boolean checkForUpdates = true;

    private String timeFormat = "yyyy-MM-dd HH:mm:ss";

    private boolean metrics = true;

    @Comment({
        "Possible options:",
        " ",
        "Remote databases - requires connection information to be configured below",
        "- MySQL",
        "- MariaDB",
        "- PostgreSQL",
        " ",
        "Flatfile/local database - no extra configuration required",
        "- SQLite (default)",
        "- H2"
    })
    private DatabaseType storageMethod = DatabaseType.SQLITE;

    private DatabaseConfig database = new DatabaseConfig();

    private ReportsConfig reports = new ReportsConfig();

    private SecurityConfig security = new SecurityConfig();

    private DiscordConfig discord = new DiscordConfig();

    private DebugConfig debug = new DebugConfig();

    private static class DatabaseTypeSerializer implements Serializer<DatabaseType, String> {
        @Override
        public String serialize(final DatabaseType element) {
            return element.name().toLowerCase();
        }

        @Override
        public DatabaseType deserialize(final String element) {
            try {
                return DatabaseType.valueOf(element.toUpperCase());
            } catch (final IllegalArgumentException e) {
                // Fallback to default if invalid value
                return DatabaseType.SQLITE;
            }
        }
    }

    @Getter
    @Setter
    @Configuration
    @NoArgsConstructor
    public static class DatabaseConfig {
        private String host = "localhost";
        private int port = 3306;
        @Comment("The name of the database to connect to.")
        private String database = "snowreports";
        private String username = "username";
        private String password = "password";

        @Comment({
            "These settings apply to the remote database connection pool.",
            " ",
            "- The default values will be suitable for the majority of users.",
            "- Do not change these settings unless you know what you're doing!"
        })
        private PoolSettingsConfig poolSettings = new PoolSettingsConfig();

        private AdvancedConfig advanced = new AdvancedConfig();

        @Getter
        @Setter
        @Configuration
        @NoArgsConstructor
        public static class PoolSettingsConfig {
            private int maximumPoolSize = 10;
            private int minimumIdle = 10;
            private long maximumLifetime = 1800000;
            private long keepaliveTime = 0;
            private int connectionTimeout = 5000;
        }

        @Getter
        @Setter
        @Configuration
        @NoArgsConstructor
        public static class AdvancedConfig {
            @Comment({
                "- This only applies for remote storage methods (MySQL, MariaDB, PostgreSQL).",
                "- Change this if you want to use the same database for multiple servers."
            })
            private String tablePrefix = "snowreports_";
            private boolean useSsl = false;
            private boolean verifyServerCertificate = false;
            private boolean allowPublicKeyRetrieval = true;
        }
    }

    @Getter
    @Setter
    @Configuration
    @NoArgsConstructor
    public static class ReportsConfig {
        private boolean enabled = true;
        @Comment("The cooldown in seconds before a player can report again. Set to -1 for no cooldown.")
        private int cooldown = 15;
        @Comment("The maximum number of reports a player can have Open or In Progress. Set to -1 for no limit.")
        private int playerLimit = -1;
        private boolean notifyConsole = true;
        private Reason reason = new Reason();
        private ChatHistory chatHistory = new ChatHistory();

        @Getter
        @Setter
        @Configuration
        @NoArgsConstructor
        public static class Reason {
            private boolean required = true;
            private String[] presets = { "Cheating", "Spamming", "Griefing", "Toxicity" };
        }

        @Getter
        @Setter
        @Configuration
        @NoArgsConstructor
        public static class ChatHistory {
            private boolean enabled = true;
            @Comment("How many messages to store in the report.")
            private int maxMessages = 5;
            @Comment({
                "How long to keep the chat history stored in memory.",
                "If a report is created after this time, the message will not be included in the report's chat history."
            })
            private int maxAgeSeconds = 1800;
        }
    }

    @Getter
    @Setter
    @Configuration
    @NoArgsConstructor
    public static class SecurityConfig {
        @Comment("If enabled, only the console can use /deleteallreports.")
        private boolean deleteAllReports = true;
        @Comment("If enabled, only the console can use /deleteallreportsfrom.")
        private boolean deleteAllReportsFrom = true;
    }

    @Getter
    @Setter
    @Configuration
    @NoArgsConstructor
    public static class DiscordConfig {
        private boolean enabled = false;
        private String webhookUrl = "";
        private Embed embed = new Embed();

        @Getter
        @Setter
        @Configuration
        @NoArgsConstructor
        public static class Embed {
            private String title = "SnowReports";
            private String hexColor = "#03c2fc";
        }
    }

    @Getter
    @Setter
    @Configuration
    @NoArgsConstructor
    public static class DebugConfig {
        private boolean allowSelfReport = false;
    }

    public void save() {
        YamlConfigurations.save(
            new File(dataFolder, "config.yml").toPath(), Config.class, this, properties
        );
    }

    public static void reload() {
        instance = YamlConfigurations.load(
            new File(dataFolder, "config.yml").toPath(), Config.class, properties
        );
    }

    public static void init(final File dataFolder) {
        Config.dataFolder = dataFolder;
        get();
    }

    public static Config get() {
        if (instance != null) {
            return instance;
        }

        return instance = YamlConfigurations.update(
            new File(dataFolder, "config.yml").toPath(), Config.class, properties
        );
    }
}
