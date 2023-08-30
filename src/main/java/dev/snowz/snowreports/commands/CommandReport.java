package dev.snowz.snowreports.commands;

import dev.snowz.snowreports.SnowReports;
import dev.snowz.snowreports.utils.CooldownManager;
import dev.snowz.snowreports.utils.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandReport implements CommandExecutor, TabCompleter {

    public final CooldownManager cooldownManager = new CooldownManager();
    public final FileConfiguration config = SnowReports.getInstance().getConfig();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c§l(!) §cOnly players can run this command!");
            return true;
        }

        Player reporter = (Player) sender;

        if (!config.getBoolean("reports.enabled", true)) {
            reporter.sendMessage("§c§l(!) §cReports are disabled!");
            return true;
        }

        if (args.length == 0) {
            reporter.sendMessage("§c§l(!) §cYou must specify a player!");
            return true;
        }

        Player reportedPlayer = Bukkit.getPlayerExact(args[0]);

        if (reportedPlayer == null) {
            reporter.sendMessage("§c§l(!) §cPlayer not found!");
            return true;
        }

        if (reportedPlayer.equals(reporter) && !config.getBoolean("debug.allow-self-report")) {
            reporter.sendMessage("§c§l(!) §cYou cannot report yourself!");
            return true;
        }

        Duration timeLeft = cooldownManager.getRemainingCooldown(reporter.getUniqueId());

        if (args.length == 1 && config.getBoolean("reports.require-reason", true)) {
            reporter.sendMessage("§c§l(!) §cYou must specify a reason!");
            return true;
        } else if (timeLeft.isZero() || timeLeft.isNegative()) {
            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";
            sendReport(reporter, reportedPlayer, reason);
            return true;
        } else {
            reporter.sendMessage("§c§l(!) §cYou cannot send a report for another " + timeLeft.getSeconds() + " seconds!");
            return true;
        }

    }

    public void sendReport(Player reporter, Player reportedPlayer, String reason) {

        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ssa"));

        SnowReports.getDb().insertReport(reportedPlayer.getUniqueId().toString(), reporter.getUniqueId().toString(), reason, timeStamp);

        String webhookURL = config.getString("discord-integration.webhook-url", "");
        if (config.getBoolean("discord-integration.enabled", false) && !webhookURL.isEmpty()) {
            DiscordWebhook webhook = new DiscordWebhook(webhookURL);
            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                    .setTitle(config.getString("discord-integration.embed.title", "Report"))
                    .setDescription("**" + reportedPlayer.getName() + "** has been reported for: " + reason)
                    .setFooter("Reported by: " + reporter.getName(), "https://crafatar.com/avatars/" + reporter.getUniqueId())
                    .setThumbnail("https://crafatar.com/renders/head/" + reportedPlayer.getUniqueId() + "?overlay")
                    .setColor(Color.decode(config.getString("discord-integration.embed.hex-color", "#03c2fc")));

            webhook.setUsername("SnowReports");
            webhook.addEmbed(embed);
            webhook.execute();
        }
        String notifyMessage = "§4" + reportedPlayer.getName() + "§4 has been reported by " + reporter.getName() + " for " + reason;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("snowreports.notify")) {
                player.sendMessage(notifyMessage);
            }
        }

        if (config.getBoolean("reports.notify-console")) {
            Bukkit.getConsoleSender().sendMessage(notifyMessage);
        }

        reporter.sendMessage("§aYour report has been sent!");
        if (!reporter.hasPermission("snowreports.bypass.cooldown")) {
            cooldownManager.setCooldown(reporter.getUniqueId(), Duration.ofSeconds(config.getInt("reports.cooldown"), 15));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> presets = config.getStringList("reports.reason-presets");
        if (args.length == 1) {
            List<String> onlinePlayers = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                onlinePlayers.add(player.getName());
            }
            return onlinePlayers;
        }
        if (args.length == 2) {
            if (presets.isEmpty()) {
                return null;
            } else {
                return presets;
            }
        }
        return null;
    }
}
