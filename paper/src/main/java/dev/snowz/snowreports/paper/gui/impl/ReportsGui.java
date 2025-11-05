package dev.snowz.snowreports.paper.gui.impl;

import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.common.database.entity.Report;
import dev.snowz.snowreports.common.database.entity.User;
import dev.snowz.snowreports.paper.gui.BaseGui;
import dev.snowz.snowreports.paper.gui.item.NextPageItem;
import dev.snowz.snowreports.paper.gui.item.PreviousPageItem;
import dev.snowz.snowreports.paper.gui.item.ReportItem;
import dev.snowz.snowreports.paper.gui.item.SortItem;
import dev.snowz.snowreports.paper.manager.HeadManager;
import dev.snowz.snowreports.paper.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.*;

import static dev.snowz.snowreports.paper.util.TimeUtil.formatEpochTime;

public final class ReportsGui implements BaseGui<PagedGui<Item>> {
    private static final Map<UUID, Window> activeWindows = new HashMap<>();

    private final List<Report> reports;
    private final int page;
    private final String title;

    public ReportsGui(final List<Report> reports, final int page) {
        this(reports, page, "Reports");
    }

    public ReportsGui(final List<Report> reports, final int page, final String title) {
        this.reports = reports;
        this.page = page;
        this.title = title;
    }

    @Override
    public @NonNull PagedGui<Item> create() {
        final List<Report> reportsList = new ArrayList<>(reports);

        @SuppressWarnings("unchecked") final PagedGui<Item>[] guiHolder = new PagedGui[1];

        final SortItem sortItem = new SortItem(
            reportsList, (sortMethod, sortedReports) -> {
            if (guiHolder[0] != null) {
                guiHolder[0].setContent(createReportItems(sortedReports));
            }
        }
        );

        final PagedGui<@NonNull Item> reportsGui = PagedGui.items()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# < # # * # # > #"
            )
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', new PreviousPageItem())
            .addIngredient('>', new NextPageItem())
            .addIngredient('*', sortItem)
            .setContent(createReportItems(reportsList))
            .build();

        // Store reference to the GUI
        guiHolder[0] = reportsGui;

        reportsGui.setPage(page - 1);
        return reportsGui;
    }

    /**
     * Creates report items from the given list of reports
     *
     * @param reportList List of reports to create items from
     * @return List of report items
     */
    private List<Item> createReportItems(final List<Report> reportList) {
        final List<Item> reportItems = new ArrayList<>();
        for (final Report report : reportList) {
            final User reported = report.getReported();
            final int id = report.getId();
            final ItemStack playerHead = HeadManager.getPlayerHead(UUID.fromString(reported.getUuid()));

            boolean isOnline = false;
            final Player reportedPlayer = Bukkit.getPlayer(UUID.fromString(reported.getUuid()));
            if (reportedPlayer != null && reportedPlayer.isOnline()) {
                isOnline = true;
            }

            final List<String> lore = new ArrayList<>();
            lore.add("§8§m                                                            ");
            lore.add("§7• §fReported: §e" + reported.getName() + " " + (isOnline ? "§a⏺" : "§7⏺"));
            lore.add("§7• §fReporter: §e" + report.getReporter().getName());
            lore.addAll(TextUtil.wrapText(report.getReason(), "§7• §fReason: §e", "§e   ", 50));
            lore.add("§7• §fCreated at: §e" + formatEpochTime(
                report.getCreatedAt(),
                Config.get().getTimeFormat()
            ));
            lore.add("§7• §fStatus: §e" + report.getStatus().getDisplayName());
            lore.add("§7• §fUpdated at: §e" + formatEpochTime(
                report.getLastUpdated(),
                Config.get().getTimeFormat()
            ));
            lore.add("§7• §fUpdated by: §e" + (report.getUpdatedBy() != null ? report.getUpdatedBy().getName() : "N/A"));
            lore.add("§7• §fServer: §e" + report.getServer());
            lore.add("");
            lore.add("§6Left-click §fto manage.");
            lore.add("§6Right-click §fto teleport to §e" + reported.getName() + "§6.");

            reportItems.add(new ReportItem(playerHead, "§6Report #" + id, lore, reported, id));
        }
        return reportItems;
    }

    /**
     * Opens the reports GUI for a player
     *
     * @param player The player to open the GUI for
     */
    public void open(final Player player) {
        final PagedGui<Item> gui = create();
        final Window window = Window.single()
            .setViewer(player)
            .setGui(gui)
            .setTitle(title)
            .addCloseHandler(() -> activeWindows.remove(player.getUniqueId()))
            .build();

        // Store reference to allow cleanup
        activeWindows.put(player.getUniqueId(), window);

        // Open the window
        window.open();
    }

    /**
     * Force closes all active report GUI windows
     */
    public static void closeAll() {
        for (final Window window : activeWindows.values()) {
            window.close();
        }
        activeWindows.clear();
    }
}
