package dev.snowz.snowreports.bukkit.gui.item;

import dev.snowz.snowreports.bukkit.SnowReports;
import dev.snowz.snowreports.bukkit.manager.GuiHistoryManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import static dev.snowz.snowreports.bukkit.manager.MessageManager.getMessage;

public final class ConfirmItem extends AbstractItem {

    private final int id;

    public ConfirmItem(final int id) {
        this.id = id;
    }

    @Override
    public ItemProvider getItemProvider() {
        final ItemBuilder builder = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE);
        builder.setDisplayName("§aConfirm");

        return builder;
    }

    @Override
    public void handleClick(
        @NotNull final ClickType clickType,
        @NotNull final Player player,
        @NotNull final InventoryClickEvent event
    ) {
        if (clickType.isLeftClick()) {
            SnowReports.runAsync(() -> {
                    try {
                        final boolean deleted = SnowReports.getReportDao().deleteById(id) > 0;

                        SnowReports.runSync(() -> {
                                if (deleted) {
                                    player.sendMessage(getMessage("report.deleted", id));
                                    GuiHistoryManager.previousMenu(player);
                                    GuiHistoryManager.previousMenu(player);
                                } else {
                                    player.sendMessage(getMessage("report.not_found", id));
                                }
                            }
                        );

                    } catch (final Exception e) {
                        SnowReports.getInstance().getLogger().warning("Failed to delete report with ID '" + id + "': " + e.getMessage());
                    }
                }
            );
        }
    }
}
