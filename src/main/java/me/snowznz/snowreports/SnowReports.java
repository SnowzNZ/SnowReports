package me.snowznz.snowreports;

import me.snowznz.snowreports.commands.Report;
import me.snowznz.snowreports.commands.SnowReportsCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SnowReports extends JavaPlugin {
    public FileConfiguration config = this.getConfig();

    private static SnowReports instance;

    public static SnowReports getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Instance
        instance = this;

        // Config
        config.addDefault("discord-webhook-url", "");
        config.addDefault("report-cooldown", 15);
        config.options().copyDefaults(true);
        saveConfig();

        // Warnings
        if (config.getString("discord-webhook-url").isEmpty()) {
            getLogger().warning("discord-webhook-url is not set, reports wont be sent to discord!");
        }

        // Commands
        getCommand("report").setExecutor(new Report());
        getCommand("snowreports").setExecutor(new SnowReportsCommand());

        // Tab Completion
        getCommand("snowreports").setTabCompleter(new SnowReportsCommand());

    }


}