package dev.snowz.snowreports;

import com.samjakob.spigui.SpiGUI;
import dev.snowz.snowreports.commands.CommandDeleteReport;
import dev.snowz.snowreports.commands.CommandReport;
import dev.snowz.snowreports.commands.CommandReports;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowReports extends JavaPlugin {

    private static SnowReports plugin;
    private static Database database;
    private static SpiGUI spiGUI;

    public static SnowReports getPlugin() {
        return plugin;
    }

    public static Database database() {
        return database;
    }

    public static SpiGUI gui() {
        return spiGUI;
    }

    @Override
    public void onEnable() {
        String currentVersion = Bukkit.getPluginManager().getPlugin("SnowReports").getDescription().getVersion();

        // Instance
        plugin = this;

        // bStats
        Metrics metrics = new Metrics(this, 19543);

        // Config
        saveDefaultConfig();

        // Commands
        getCommand("delreport").setExecutor(new CommandDeleteReport());
        getCommand("report").setExecutor(new CommandReport());
        getCommand("reports").setExecutor(new CommandReports());

        // Database
        database = new Database();

        // GUI
        spiGUI = new SpiGUI(this);
    }

    @Override
    public void onDisable() {
        database.safeDisconnect();
    }
}
