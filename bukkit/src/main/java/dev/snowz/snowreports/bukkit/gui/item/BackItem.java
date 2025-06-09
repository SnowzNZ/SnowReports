package dev.snowz.snowreports.bukkit.gui.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

import java.util.*;

public final class BackItem extends AbstractItem {

    private static final Map<UUID, Deque<Window>> playerWindowHistory = new HashMap<>();

    public static void pushWindow(final Player player, final Window window) {
        playerWindowHistory.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>())
            .push(window);
    }

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
        final Deque<Window> history = playerWindowHistory.get(player.getUniqueId());
        if (history != null && !history.isEmpty()) {
            final Window previousWindow = history.pop();
            previousWindow.open();
        } else {
            player.closeInventory();
        }
    }
}