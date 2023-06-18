package me.snowznz.snowreports;

import me.snowznz.snowreports.commands.Report;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class SnowReports extends JavaPlugin {
    FileConfiguration config = this.getConfig();

    @Override
    public void onEnable() {

        config.addDefault("discord-webhook-url", "");
        config.options().copyDefaults(true);
        saveConfig();

        if (Objects.equals(config.getString("discord-webhook-url"), "")) {
            getLogger().warning("discord-webhook-url is not set, reports wont be sent to discord!");
        }

        getCommand("report").setExecutor(new Report(config));


        Permission receiveReport = new Permission("snowreports.report.receive");
        getServer().getPluginManager().addPermission(receiveReport);

    }
}
