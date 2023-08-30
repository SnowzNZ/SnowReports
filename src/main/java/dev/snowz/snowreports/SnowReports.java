package dev.snowz.snowreports;

import com.samjakob.spigui.SpiGUI;
import dev.snowz.snowreports.commands.CommandDelReport;
import dev.snowz.snowreports.commands.CommandReport;
import dev.snowz.snowreports.commands.CommandReports;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowReports extends JavaPlugin {

    private static SnowReports instance;
    private static Database database;
    private static SpiGUI spiGUI;

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

        // Instance
        plugin = this;

        if (getConfig().getBoolean("metrics")) {
            Metrics metrics = new Metrics(this, 19543);
        }

        saveDefaultConfig();

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
}
