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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CommandReports implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

        Player player = (Player) sender;

        if (args.length == 1) {
            Player queriedPlayer = Bukkit.getPlayer(args[0]);
            List<Report> reports = SnowReports.getDb().getPlayerReports(queriedPlayer.getUniqueId().toString());
            reports.sort(Comparator.comparingInt(Report::getReportID).reversed());

            if (reports.isEmpty()) {
                sender.sendMessage("§a§l" + queriedPlayer.getName() + " §ahas no reports!");
                return true;
            }

            SGMenu reportsMenu = SnowReports.getSpiGUI().create(queriedPlayer.getName() + "'s Reports &7(Page {currentPage}/{maxPage})", 6);

            for (Report report : reports) {

                Player reporter = Bukkit.getPlayer(UUID.fromString(report.getReporterUUID()));

                SGButton button = new SGButton(
                        new ItemBuilder(Material.PAPER)
                                .name("§c" + queriedPlayer.getName() + " §7(ID: " + report.getReportID() + ")")
                                .lore(
                                        "",
                                        "§b‣ §fReported by: §a" + reporter.getName(),
                                        "§b‣ §fReason: §e" + report.getReason(),
                                        "",
                                        "§b‣ §fDatetime: §7" + report.getTimeStamp(),
                                        "",
                                        "§7Left-click to teleport to §c" + queriedPlayer.getName(),
                                        "§7Right-click to §c§ldelete §7this report"
                                ).build())
                        .withListener((InventoryClickEvent event) -> {
                            if (event.isLeftClick()) {
                                event.getWhoClicked().teleport(queriedPlayer.getLocation());
                            } else if (event.isRightClick()) {
                                SnowReports.getDb().deleteReport(report.getReportID());
                                reportsMenu.removeButton(event.getSlot());
                                reportsMenu.refreshInventory(event.getWhoClicked());
                            }
                        });

                reportsMenu.addButton(button);
            }

            player.openInventory(reportsMenu.getInventory());

        } else if (args.length == 0) {
            List<Report> reports = SnowReports.getDb().getAllReports();
            reports.sort(Comparator.comparingInt(Report::getReportID).reversed());

            if (reports.isEmpty()) {
                sender.sendMessage("§aThere are no reports!");
                return true;
            }

            SGMenu reportsMenu = SnowReports.getSpiGUI().create("Global Reports &7(Page {currentPage}/{maxPage})", 6);

            for (Report report : reports) {
                Player reportedPlayer = Bukkit.getPlayer(UUID.fromString(report.getReportedPlayerUUID()));

                ItemStack playerSkull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta playerSkullMeta = (SkullMeta) playerSkull.getItemMeta();
                playerSkullMeta.setOwner(reportedPlayer.getName());
                playerSkull.setItemMeta(playerSkullMeta);

                Player reporter = Bukkit.getPlayer(UUID.fromString(report.getReporterUUID()));

                SGButton button = new SGButton(
                        new ItemBuilder(playerSkull)
                                .name("§c" + reportedPlayer.getName() + " §7(ID: " + report.getReportID() + ")")
                                .lore(
                                        "",
                                        "§b‣ §fReported by: §a" + reporter.getName(),
                                        "§b‣ §fReason: §e" + report.getReason(),
                                        "",
                                        "§b‣ §fDatetime: §7" + report.getTimeStamp(),
                                        "",
                                        "§7Left-click to teleport to §c" + reportedPlayer.getName(),
                                        "§7Right-click to §c§ldelete §7this report"
                                ).build()
                ).withListener((InventoryClickEvent event) -> {
                    if (event.isLeftClick()) {
                        event.getWhoClicked().teleport(reportedPlayer.getLocation());
                    } else if (event.isRightClick()) {
                        SnowReports.getDb().deleteReport(report.getReportID());
                        reportsMenu.removeButton(event.getSlot());
                        reportsMenu.refreshInventory(event.getWhoClicked());
                    }
                });

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
