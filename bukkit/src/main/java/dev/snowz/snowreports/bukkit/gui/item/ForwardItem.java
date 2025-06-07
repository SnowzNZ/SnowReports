package dev.snowz.snowreports.bukkit.gui.item;

import org.bukkit.Material;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;

public final class ForwardItem extends PageItem {

    public ForwardItem() {
        super(true);
    }

    @Override
    public ItemProvider getItemProvider(final PagedGui<?> gui) {
        final ItemBuilder builder = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE);
        builder.setDisplayName("§aForward")
            .addLoreLines(gui.hasNextPage()
                ? (gui.getCurrentPage() + 2) + "/" + gui.getPageAmount()
                : "There are no more pages");

        return builder;
    }
}
