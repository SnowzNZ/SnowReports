package dev.snowz.snowreports.bukkit.gui.item;

import dev.snowz.snowreports.bukkit.gui.impl.DeleteReportConfirmationGui;
import dev.snowz.snowreports.bukkit.manager.GuiHistoryManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public final class DeleteItem extends AbstractItem {

    private final int id;

    public DeleteItem(final int id) {
        this.id = id;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.BARRIER)
            .setDisplayName("§4Delete Report");
    }

    @Override
    public void handleClick(
        @NotNull final ClickType clickType,
        @NotNull final Player player,
        @NotNull final InventoryClickEvent event
    ) {
        GuiHistoryManager.pushCurrentWindow(player, getWindows());
        new DeleteReportConfirmationGui(id).open(player);
    }
}
