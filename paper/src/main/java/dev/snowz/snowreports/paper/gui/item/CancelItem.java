package dev.snowz.snowreports.paper.gui.item;

import dev.snowz.snowreports.paper.manager.GuiHistoryManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public final class CancelItem extends AbstractItem {

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
            .setDisplayName("§cCancel");
    }

    @Override
    public void handleClick(
        @NonNull final ClickType clickType,
        @NonNull final Player player,
        @NonNull final InventoryClickEvent event
    ) {
        if (clickType.isLeftClick()) {
            GuiHistoryManager.previousMenu(player);
        }
    }
}
