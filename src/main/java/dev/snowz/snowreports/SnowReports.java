package dev.snowz.snowreports;

import com.samjakob.spigui.SpiGUI;
import dev.snowz.snowreports.commands.CommandDelReport;
import dev.snowz.snowreports.commands.CommandReport;
import dev.snowz.snowreports.commands.CommandReports;
import dev.snowz.snowreports.commands.CommandSnowReports;
import dev.snowz.snowreports.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

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

        UpdateChecker.checkUpdates(getDescription().getVersion());

        if (getConfig().getBoolean("metrics")) {
            new Metrics(this, 19543);
        }

        saveDefaultConfig();

        getCommand("delreport").setExecutor(new CommandDelReport());
        getCommand("report").setExecutor(new CommandReport());
        getCommand("reports").setExecutor(new CommandReports());
        getCommand("snowreports").setExecutor(new CommandSnowReports());


        database = new Database();

        spiGUI = new SpiGUI(this);
    }

    @Override
    public void onDisable() {
        database.safeDisconnect();
    }

    public String getLocale(Player player) {
        String locale;
        String serverVersion = Bukkit.getBukkitVersion();
        String[] versionParts = serverVersion.split("-")[0].split("\\.");
        int majorVersion = Integer.parseInt(versionParts[1]);
        if (majorVersion < 12) {
            locale = player.spigot().getLocale();
        } else {
            try {
                locale = player.getClass().getMethod("getLocale").invoke(player).toString();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        // return new Locale(locale.split("_")[0], locale.split("_")[1]);
        return locale;
    }
}
