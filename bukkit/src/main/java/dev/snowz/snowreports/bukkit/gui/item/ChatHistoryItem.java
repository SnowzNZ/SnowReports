package dev.snowz.snowreports.bukkit.gui.item;

import dev.snowz.snowreports.api.model.ChatMessage;
import dev.snowz.snowreports.common.config.Config;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Collections;

import static dev.snowz.snowreports.bukkit.util.TimeUtil.formatEpochTime;

public final class ChatHistoryItem extends AbstractItem {

    private final ChatMessage chatMessage;

    public ChatHistoryItem(final ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.PAPER)
            .setDisplayName(formatEpochTime(chatMessage.timestamp(), Config.get().getTimeFormat()))
            .setLegacyLore(Collections.singletonList(chatMessage.message()));
    }

    @Override
    public void handleClick(
        @NotNull final ClickType clickType,
        @NotNull final Player player,
        @NotNull final InventoryClickEvent event
    ) {
    }
}
