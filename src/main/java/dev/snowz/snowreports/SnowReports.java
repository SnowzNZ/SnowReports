package dev.snowz.snowreports;

import com.samjakob.spigui.SpiGUI;
import dev.snowz.snowreports.commands.CommandDelReport;
import dev.snowz.snowreports.commands.CommandReport;
import dev.snowz.snowreports.commands.CommandReports;
import dev.snowz.snowreports.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SnowReports extends JavaPlugin {

    private static SnowReports instance;
    private static Database database;
    private static SpiGUI spiGUI;
    private static YamlConfiguration messagesConfig;

    public static YamlConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public static SnowReports getInstance() {
        return instance;
    }

    public static Database getDb() {
        return database;
    }

    public static SpiGUI getSpiGUI() {
        return spiGUI;
    }

    @Override
    public void onEnable() {
        instance = this;

        UpdateChecker.checkUpdates(getDescription().getVersion());

        if (getConfig().getBoolean("metrics")) {
            new Metrics(this, 19543);
        }

        saveDefaultConfig();
        loadMessagesConfig();

        getCommand("delreport").setExecutor(new CommandDelReport());
        getCommand("report").setExecutor(new CommandReport());
        getCommand("reports").setExecutor(new CommandReports());

        database = new Database();

        spiGUI = new SpiGUI(this);
    }

    @Override
    public void onDisable() {
        database.safeDisconnect();
    }

    private void loadMessagesConfig() {
        File messagesConfigFile = new File(getDataFolder(), "messages.yml");
        if (!messagesConfigFile.exists()) {
            saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile);
    }
}
