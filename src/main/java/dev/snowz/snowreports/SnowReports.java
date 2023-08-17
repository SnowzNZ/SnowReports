package dev.snowz.snowreports;

import dev.snowz.snowreports.commands.Commanddelreport;
import dev.snowz.snowreports.commands.Commandreport;
import dev.snowz.snowreports.commands.Commandreports;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SnowReports extends JavaPlugin {

    private static SnowReports instance;
    private static Connection connection;
    private final FileConfiguration config = getConfig();

    public static SnowReports getInstance() {
        return instance;
    }

    public static Connection getConnection() {
        return connection;
    }

    @Override
    public void onEnable() {
        // Instance
        instance = this;

        // bStats Metrics
        int pluginId = 19543;
        Metrics metrics = new Metrics(this, pluginId);

        // Config
        saveDefaultConfig();

        // Checks
        if (config.getBoolean("discord-integration.enabled") && config.getString("discord-integration.webhook-url").isEmpty()) {
            getLogger().info("Discord Integration is enabled but webhook-url is not set!");
            config.set("discord-integration.enabled", false);
            reloadConfig();
        }

        // Commands
        getCommand("delreport").setExecutor(new Commanddelreport());
        getCommand("report").setExecutor(new Commandreport());
        getCommand("reports").setExecutor(new Commandreports());

        // Database
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + getDataFolder() + File.separator + "data.db");
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(30);
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS Reports (reportID INT, reportedPlayerUUID TEXT, reporterUUID TEXT, reason TEXT, timeStamp TEXT)");
            } catch (SQLException e) {
                getLogger().severe("Error executing SQL query: " + e.getMessage());
            }
        } catch (SQLException e) {
            getLogger().severe("Error connecting to the database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
