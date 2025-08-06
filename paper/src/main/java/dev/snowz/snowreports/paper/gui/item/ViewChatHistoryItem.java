package dev.snowz.snowreports.paper.gui.item;

import dev.snowz.snowreports.paper.gui.impl.ChatHistoryGui;
import dev.snowz.snowreports.paper.manager.GuiHistoryManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public final class ViewChatHistoryItem extends AbstractItem {
    private final int id;

    public ViewChatHistoryItem(final int id) {
        this.id = id;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.WRITABLE_BOOK)
            .setDisplayName("§6View Chat History");
    }

    @Override
    public void handleClick(
        @NotNull final ClickType clickType,
        @NotNull final Player player,
        @NotNull final InventoryClickEvent event
    ) {
        GuiHistoryManager.pushCurrentWindow(player, getWindows());
        new ChatHistoryGui(id).open(player);
    }
}
