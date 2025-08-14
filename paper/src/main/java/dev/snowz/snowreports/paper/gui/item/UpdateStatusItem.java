package dev.snowz.snowreports.paper.gui.item;

import dev.snowz.snowreports.common.database.entity.Report;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.gui.impl.UpdateStatusGui;
import dev.snowz.snowreports.paper.manager.GuiHistoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Collections;
import java.util.Objects;

public final class UpdateStatusItem extends AbstractItem {

    private final int id;
    private final Report report;

    public UpdateStatusItem(final int id) {
        this.id = id;
        this.report = Objects.requireNonNull(SnowReports.getReportManager().getReportById(id));
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(report.getStatus().getMaterial())
            .setDisplayName("§6Update Status")
            .setLegacyLore(Collections.singletonList("§7• §fCurrent Status: " + report.getStatus().getDisplayName()));
    }

    @Override
    public void handleClick(
        @NonNull final ClickType clickType,
        @NonNull final Player player,
        @NonNull final InventoryClickEvent event
    ) {
        GuiHistoryManager.pushCurrentWindow(player, getWindows());
        new UpdateStatusGui(id).open(player);
    }
}
