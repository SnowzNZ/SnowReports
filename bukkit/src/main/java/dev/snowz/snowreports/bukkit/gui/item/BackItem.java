package dev.snowz.snowreports.bukkit.gui.item;

import dev.snowz.snowreports.bukkit.gui.manager.GuiHistoryManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public final class BackItem extends AbstractItem {

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.ARROW)
            .setDisplayName("§dBack");
    }

    @Override
    public void handleClick(
        @NotNull final ClickType clickType,
        @NotNull final Player player,
        @NotNull final InventoryClickEvent inventoryClickEvent
    ) {
        GuiHistoryManager.previousMenu(player);
    }
}
