package me.snowznz.snowreports.commands;

import me.snowznz.snowreports.utils.DiscordWebhook;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class Report implements CommandExecutor {
    private final FileConfiguration config;

    public Report(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player reporter)) {
            sender.sendMessage("Only players can run this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /report <player> <reason>");
            return true;
        }

        Player reportedPlayer = Bukkit.getPlayer(args[0]);
        if (reportedPlayer == null) {
            sender.sendMessage("Player not found!");
            return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (reporter.hasPermission("snowreports.report")) {
            if (!(Objects.equals(config.getString("discord-webhook-url"), ""))) {
                DiscordWebhook webhook = new DiscordWebhook(config.getString("discord-webhook-url"));
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setDescription("**" + reportedPlayer.getName() + "** has been reported for: " + reason)
                        .setFooter("Reported by: " + reporter.getName(), "https://crafatar.com/avatars/" + reporter.getUniqueId())
                        .setImage("https://crafatar.com/renders/body/" + reportedPlayer.getUniqueId() + "?overlay=true"));
                webhook.setUsername("SnowReports");

                webhook.execute();
            }

            reporter.sendMessage(ChatColor.GREEN + "Your report has been sent!");

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("snowreports.report.receive")) {
                    TextComponent message = new TextComponent(ChatColor.RED + reportedPlayer.getName() + " has been reported for: " + reason);
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Reported by " + reporter.getName() + ". Click to teleport to " + reportedPlayer.getName())));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + reportedPlayer.getName()));
                    admin.spigot().sendMessage(message);
                }
            }

            return true;
        }

        return false;
    }


}
