package dev.snowz.snowreports.bukkit.gui.item;

import dev.snowz.snowreports.bukkit.SnowReports;
import dev.snowz.snowreports.bukkit.gui.impl.UpdateStatusGui;
import dev.snowz.snowreports.bukkit.gui.manager.GuiHistoryManager;
import dev.snowz.snowreports.common.database.entity.Report;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

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
        @NotNull final ClickType clickType,
        @NotNull final Player player,
        @NotNull final InventoryClickEvent event
    ) {
        GuiHistoryManager.pushCurrentWindow(player, getWindows());
        new UpdateStatusGui(id).open(player);
    }
}
