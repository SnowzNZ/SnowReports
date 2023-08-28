package dev.snowz.snowreports.commands;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import dev.snowz.snowreports.Report;
import dev.snowz.snowreports.SnowReports;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CommandReports implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

        Player player = (Player) sender;
        ItemStack playerSkull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta playerSkullMeta = (SkullMeta) playerSkull.getItemMeta();

        if (args.length == 1) {
            Player queriedPlayer = Bukkit.getPlayer(args[0]);
            List<Report> reports = SnowReports.database().getPlayerReports(queriedPlayer.getUniqueId().toString());
            reports.sort(Comparator.comparingInt(Report::getReportID).reversed());

            if (reports.isEmpty()) {
                sender.sendMessage("§a" + queriedPlayer.getName() + " has no reports!");
                return true;
            }

            SGMenu reportsMenu = SnowReports.gui().create("&c&l" + queriedPlayer.getName() + "'s Reports", 6);

            for (Report report : reports) {
                playerSkullMeta.setOwner(queriedPlayer.getName());
                playerSkull.setItemMeta(playerSkullMeta);

                Player reporter = Bukkit.getPlayer(UUID.fromString(report.getReporterUUID()));

                SGButton button = new SGButton(
                        new ItemBuilder(playerSkull)
                                .name(queriedPlayer.getName())
                                .lore(
                                        "Reporter: " + reporter.getName(),
                                        "Reason: " + report.getReason(),
                                        "Time: " + report.getTimeStamp()
                                ).build());

                reportsMenu.addButton(button);
            }

            player.openInventory(reportsMenu.getInventory());

        } else if (args.length == 0) {
            List<Report> reports = SnowReports.database().getAllReports();
            reports.sort(Comparator.comparingInt(Report::getReportID).reversed());

            if (reports.isEmpty()) {
                sender.sendMessage("§aThere are no reports!");
                return true;
            }

            SGMenu reportsMenu = SnowReports.gui().create("&c&lRecent Reports", 6);

            for (Report report : reports) {
                Player reportedPlayer = Bukkit.getPlayer(UUID.fromString(report.getReportedPlayerUUID()));

                playerSkullMeta.setOwner(reportedPlayer.getName());
                playerSkull.setItemMeta(playerSkullMeta);

                Player reporter = Bukkit.getPlayer(UUID.fromString(report.getReporterUUID()));

                SGButton button = new SGButton(
                        new ItemBuilder(playerSkull)
                                .name(reportedPlayer.getName())
                                .lore(
                                        "Reporter: " + reporter.getName(),
                                        "Reason: " + report.getReason(),
                                        "Time: " + report.getTimeStamp()
                                ).build());

                reportsMenu.addButton(button);
            }

            player.openInventory(reportsMenu.getInventory());
        }
        return true;
    }

}

//        int pageSize = 3; // Set the number of reports per page
//        int pageCount = (int) Math.ceil((double) reports.size() / pageSize);
//
//        for (int page = 1; page <= pageCount; page++) {
//            sender.sendMessage("Page " + page + "/" + pageCount);
//            sender.sendMessage("§m------------------------------");
//
//            int startIndex = (page - 1) * pageSize;
//            int endIndex = Math.min(startIndex + pageSize, reports.size());
//
//            for (int i = startIndex; i < endIndex; i++) {
//                Report report = reports.get(i);
//                OfflinePlayer reporter = Bukkit.getOfflinePlayer(UUID.fromString(report.getReporterUUID()));
//
//                sender.sendMessage("§eReport ID: " + report.getReportID());
//                sender.sendMessage("§aReporter: " + reporter.getName());
//                sender.sendMessage("§cReason: " + report.getReason());
//                sender.sendMessage("§bDate/Time: " + report.getTimeStamp());
//                sender.sendMessage("§m------------------------------");
//            }
//        }
