package dev.snowz.snowreports.commands;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.menu.SGMenu;
import dev.snowz.snowreports.Report;
import dev.snowz.snowreports.SnowReports;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ReportsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                int pageSize = 5;
                List<Report> reports = SnowReports.getDb().getAllReports();
                reports.sort(Comparator.comparingInt(Report::getReportID).reversed());

                int pageCount = (int) Math.ceil((double) reports.size() / pageSize);

                for (int page = 1; page <= pageCount; page++) {
                    sender.sendMessage("Page " + page + "/" + pageCount);
                    sender.sendMessage("§m------------------------------");

                    int startIndex = (page - 1) * pageSize;
                    int endIndex = Math.min(startIndex + pageSize, reports.size());

                    for (int i = startIndex; i < endIndex; i++) {
                        Report report = reports.get(i);
                        OfflinePlayer reporter = Bukkit.getOfflinePlayer(UUID.fromString(report.getReporterUUID()));
                        OfflinePlayer reportedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(report.getReportedPlayerUUID()));

                        sender.sendMessage("§7Report ID: " + report.getReportID());
                        sender.sendMessage("§cPlayer: " + reportedPlayer.getName());
                        sender.sendMessage("§aReporter: " + reporter.getName());
                        sender.sendMessage("§eReason: " + report.getReason());
                        sender.sendMessage("§bTimestamp: " + report.getTimeStamp());
                        sender.sendMessage("§m------------------------------");
                    }
                }
            } else if (args.length == 1) {
                OfflinePlayer queriedPlayer = Bukkit.getOfflinePlayer(args[0]);
                int pageSize = 5;
                List<Report> reports = SnowReports.getDb().getPlayerReports(queriedPlayer.getUniqueId().toString());
                reports.sort(Comparator.comparingInt(Report::getReportID).reversed());

                int pageCount = (int) Math.ceil((double) reports.size() / pageSize);

                for (int page = 1; page <= pageCount; page++) {
                    sender.sendMessage("Page " + page + "/" + pageCount);
                    sender.sendMessage("§m------------------------------");

                    int startIndex = (page - 1) * pageSize;
                    int endIndex = Math.min(startIndex + pageSize, reports.size());

                    for (int i = startIndex; i < endIndex; i++) {
                        Report report = reports.get(i);
                        OfflinePlayer reporter = Bukkit.getOfflinePlayer(UUID.fromString(report.getReporterUUID()));

                        sender.sendMessage("§7Report ID: " + report.getReportID());
                        sender.sendMessage("§cPlayer: " + queriedPlayer.getName());
                        sender.sendMessage("§aReporter: " + reporter.getName());
                        sender.sendMessage("§eReason: " + report.getReason());
                        sender.sendMessage("§bTimestamp: " + report.getTimeStamp());
                        sender.sendMessage("§m------------------------------");
                    }
                }
            }
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            Player queriedPlayer = Bukkit.getPlayer(args[0]);

            if (queriedPlayer == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            List<Report> reports = SnowReports.getDb().getPlayerReports(queriedPlayer.getUniqueId().toString());
            reports.sort(Comparator.comparingInt(Report::getReportID).reversed());

            if (reports.isEmpty()) {
                sender.sendMessage("§a§l" + queriedPlayer.getName() + " §ahas no reports!");
                return true;
            }

            SGMenu reportsMenu = createReportsMenu("Reports for " + queriedPlayer.getName(), reports, player);

            player.openInventory(reportsMenu.getInventory());

        } else if (args.length == 0) {
            List<Report> reports = SnowReports.getDb().getAllReports();
            reports.sort(Comparator.comparingInt(Report::getReportID).reversed());

            if (reports.isEmpty()) {
                sender.sendMessage("§aThere are no reports!");
                return true;
            }

            SGMenu reportsMenu = createReportsMenu("Global Reports", reports, player);

            player.openInventory(reportsMenu.getInventory());
        }
        return true;
    }

    private SGMenu createReportsMenu(String title, List<Report> reports, Player player) {
        SGMenu reportsMenu = SnowReports.getSpiGUI().create(title + " &7(Page {currentPage}/{maxPage})", 6);

        for (Report report : reports) {
            Player reportedPlayer = Bukkit.getPlayer(UUID.fromString(report.getReportedPlayerUUID()));

            ItemStack playerSkull = new ItemBuilder(Material.SKULL_ITEM)
                    .skullOwner(reportedPlayer.getName())
                    .build();

            Player reporter = Bukkit.getPlayer(UUID.fromString(report.getReporterUUID()));

            SGButton button = new SGButton(
                    new ItemBuilder(playerSkull)
                            .name("§c" + reportedPlayer.getName() + " §7(ID: " + report.getReportID() + ")")
                            .lore(
                                    "",
                                    "§b‣ §fReported by: §a" + reporter.getName(),
                                    "§b‣ §fReason: §e" + report.getReason(),
                                    "",
                                    "§b‣ §fTimestamp: §7" + report.getTimeStamp(),
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

        return reportsMenu;
    }
}
