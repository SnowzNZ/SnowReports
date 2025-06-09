package dev.snowz.snowreports.bukkit.gui.item;

import org.bukkit.Material;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;

public final class PreviousPageItem extends PageItem {

    public PreviousPageItem() {
        super(false);
    }

    @Override
    public ItemProvider getItemProvider(final PagedGui<?> gui) {
        final ItemBuilder builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE);
        builder.setDisplayName("§cPrevious Page")
            .addLoreLines(gui.hasPreviousPage()
                ? gui.getCurrentPage() + "/" + gui.getPageAmount()
                : "No previous page");

        return builder;
    }
}
