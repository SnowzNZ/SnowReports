package me.snowznz.snowreports.commands;

import me.snowznz.snowreports.SnowReports;
import me.snowznz.snowreports.utils.CooldownManager;
import me.snowznz.snowreports.utils.DiscordWebhook;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Duration;
import java.util.Arrays;

public class Report implements CommandExecutor {

    private final FileConfiguration config = SnowReports.getInstance().getConfig();
    private final CooldownManager cooldownManager = new CooldownManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c§l(!) §cOnly players can run this command!");
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        Player reporter = (Player) sender;
        Player reportedPlayer = Bukkit.getPlayerExact(args[0]);

        if (reportedPlayer == null) {
            sender.sendMessage("§c§l(!) §cPlayer not found!");
            return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (reporter.hasPermission("snowreports.bypass.cooldown")) {
            sendReport(reporter, reportedPlayer, reason);
        } else {
            Duration timeLeft = cooldownManager.getRemainingCooldown(reporter.getUniqueId());

            if (timeLeft.isZero() || timeLeft.isNegative()) {
                sendReport(reporter, reportedPlayer, reason);
            } else {
                reporter.sendMessage("§c§l(!) §cYou cannot run this command for another " + timeLeft.getSeconds() + " seconds!");
            }
        }

        return true;
    }

    private void sendReport(Player reporter, Player reportedPlayer, String reason) {
        String webhookUrl = config.getString("discord-webhook-url");

        if (!webhookUrl.isEmpty()) {
            DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Report")
                    .setDescription("**" + reportedPlayer.getName() + "** has been reported for: " + reason)
                    .setFooter("Reported by: " + reporter.getName(), "https://crafatar.com/avatars/" + reporter.getUniqueId())
                    .setThumbnail("https://crafatar.com/renders/head/" + reportedPlayer.getUniqueId() + "?overlay")
                    .setColor(new Color(0, 255, 255)));
            webhook.setUsername("SnowReports");

            webhook.execute();
        }

        reporter.sendMessage("§aYour report has been sent!");
        cooldownManager.setCooldown(reporter.getUniqueId(), Duration.ofSeconds(config.getInt("report-cooldown")));

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("snowreports.report.receive")) {
                TextComponent message = new TextComponent("§4§l§n" + reportedPlayer.getName() + "§4 has been reported for: " + reason);
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Reported by " + reporter.getName() + ". Click to teleport to " + reportedPlayer.getName()).create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + reportedPlayer.getName()));
                admin.spigot().sendMessage(message);
            }
        }
    }
}
