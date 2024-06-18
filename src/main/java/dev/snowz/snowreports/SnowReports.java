package dev.snowz.snowreports;

import com.samjakob.spigui.SpiGUI;
import dev.snowz.snowreports.commands.DelReportCommand;
import dev.snowz.snowreports.commands.ReportCommand;
import dev.snowz.snowreports.commands.ReportsCommand;
import dev.snowz.snowreports.commands.SnowReportsCommand;
import dev.snowz.snowreports.util.UpdateChecker;
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

        if (getConfig().getBoolean("check-for-updates")) {
            UpdateChecker.checkUpdates(getDescription().getVersion());
        }

        if (getConfig().getBoolean("metrics")) {
            new Metrics(this, 19543);
        }

        saveDefaultConfig();

        getCommand("delreport").setExecutor(new DelReportCommand());
        getCommand("report").setExecutor(new ReportCommand());
        getCommand("reports").setExecutor(new ReportsCommand());
        getCommand("snowreports").setExecutor(new SnowReportsCommand());


        database = new Database();

        spiGUI = new SpiGUI(this);
    }

    @Override
    public void onDisable() {
        database.safeDisconnect();
    }
}
