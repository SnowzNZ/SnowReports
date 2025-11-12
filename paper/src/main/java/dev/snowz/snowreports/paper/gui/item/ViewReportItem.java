package dev.snowz.snowreports.paper.gui.item;

import dev.snowz.snowreports.api.model.ReportStatus;
import dev.snowz.snowreports.common.database.entity.User;
import dev.snowz.snowreports.paper.SnowReports;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

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
        if (clickType.isLeftClick()) {
            SnowReports.getReportManager().updateReportStatus(id, ReportStatus.RESOLVED, player);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            player.closeInventory();
            player.sendMessage(getMessage("report.resolved"));
        }
    }

    @Override
    public ItemProvider getItemProvider() {
        final ItemBuilder builder = new ItemBuilder(playerHead);
        builder.setDisplayName(displayName)
            .setLegacyLore(lore);

        return builder;
    }
}
