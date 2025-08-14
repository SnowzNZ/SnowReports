package dev.snowz.snowreports.paper.gui.item;

import dev.snowz.snowreports.api.model.ReportStatus;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.manager.GuiHistoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public final class OpenItem extends AbstractItem {
    private final ReportStatus status = ReportStatus.OPEN;

    private final int id;

    public OpenItem(final int id) {
        this.id = id;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(status.getMaterial())
            .setDisplayName(status.getDisplayName());
    }

    @Override
    public void handleClick(
        @NonNull final ClickType clickType,
        @NonNull final Player player,
        @NonNull final InventoryClickEvent event
    ) {
        SnowReports.getReportManager().updateReportStatus(id, status, player);
        GuiHistoryManager.previousMenu(player);
    }
}
