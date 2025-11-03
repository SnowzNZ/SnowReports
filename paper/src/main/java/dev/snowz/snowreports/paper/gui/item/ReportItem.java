package dev.snowz.snowreports.paper.gui.item;

import dev.snowz.snowreports.common.database.entity.User;
import dev.snowz.snowreports.paper.gui.impl.ManageGui;
import dev.snowz.snowreports.paper.manager.GuiHistoryManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;
import java.util.UUID;

import static dev.snowz.snowreports.paper.manager.MessageManager.getMessage;

public final class ReportItem extends AbstractItem {

    private final ItemStack playerHead;
    private final String displayName;
    private final List<String> lore;
    private final User reported;
    private final int id;

    public ReportItem(
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
            GuiHistoryManager.pushCurrentWindow(player, getWindows());
            new ManageGui(this, id).open(player);
        } else if (clickType.isRightClick()) {
            final Player target = Bukkit.getPlayer(UUID.fromString(reported.getUuid()));
            if (target == null) {
                player.sendMessage(getMessage("player.not_online", reported.getName()));
                return;
            }
            player.teleport(target);
            player.sendMessage(getMessage("player.teleported_to", reported.getName()));
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
