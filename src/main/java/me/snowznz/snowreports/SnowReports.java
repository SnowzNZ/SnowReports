package me.snowznz.snowreports;

import me.snowznz.snowreports.commands.Reload;
import me.snowznz.snowreports.commands.Report;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SnowReports extends JavaPlugin {
    private static SnowReports instance;
    FileConfiguration config = this.getConfig();

    public static SnowReports getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        config.addDefault("discord-webhook-url", "");
        config.addDefault("report-cooldown", 15);
        config.options().copyDefaults(true);
        saveConfig();

        if (Objects.equals(config.getString("discord-webhook-url"), "")) {
            getLogger().warning("discord-webhook-url is not set, reports wont be sent to discord!");
        }

        getCommand("report").setExecutor(new Report());
        getCommand("snowreports").setExecutor(new Reload());
        getCommand("snowreports").setTabCompleter(new Reload());

    }


}