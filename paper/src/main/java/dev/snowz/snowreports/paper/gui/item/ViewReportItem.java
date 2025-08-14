package dev.snowz.snowreports.paper.gui.item;

import dev.snowz.snowreports.common.database.entity.User;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;

public final class ViewReportItem extends AbstractItem {

    private final ItemStack playerHead;
    private final String displayName;
    private final List<String> lore;
    private final User reported;
    private final int id;

    public ViewReportItem(
        final ItemStack playerHead,
        final String displayName,
        final List<String> lore,
        final User reported,
        final int id
    ) {
        this.playerHead = playerHead;
        this.displayName = displayName;
        this.lore = lore;
        this.reported = reported;
        this.id = id;
    }

    @Override
    public void handleClick(
        @NonNull final ClickType clickType,
        @NonNull final Player player,
        @NonNull final InventoryClickEvent event
    ) {
    }

    @Override
    public ItemProvider getItemProvider() {
        final ItemBuilder builder = new ItemBuilder(playerHead);
        builder.setDisplayName(displayName)
            .setLegacyLore(lore);

        return builder;
    }
}
