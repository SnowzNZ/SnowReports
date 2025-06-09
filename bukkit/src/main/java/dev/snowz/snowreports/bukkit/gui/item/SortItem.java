package dev.snowz.snowreports.bukkit.gui.item;

import dev.snowz.snowreports.common.database.entity.Report;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class SortItem extends AbstractItem {

    @Getter
    public enum SortMethod {
        // Date sort options
        DATE_ASC(
            "Date: Oldest to Newest", (reports, reversed) ->
            reports.sort(Comparator.comparing(Report::getCreatedAt))
        ),
        DATE_DESC(
            "Date: Newest to Oldest", (reports, reversed) ->
            reports.sort(Comparator.comparing(Report::getCreatedAt).reversed())
        ),

        // Reporter name sort options
        REPORTER_NAME_ASC(
            "Reporter: A to Z", (reports, reversed) ->
            reports.sort(Comparator.comparing((Report r) -> r.getReporter().getName(), String.CASE_INSENSITIVE_ORDER))
        ),
        REPORTER_NAME_DESC(
            "Reporter: Z to A", (reports, reversed) ->
            reports.sort(Comparator.comparing(
                (Report r) -> r.getReporter().getName(),
                String.CASE_INSENSITIVE_ORDER
            ).reversed())
        ),

        // Reported name sort options
        REPORTED_NAME_ASC(
            "Reported: A to Z", (reports, reversed) ->
            reports.sort(Comparator.comparing((Report r) -> r.getReported().getName(), String.CASE_INSENSITIVE_ORDER))
        ),
        REPORTED_NAME_DESC(
            "Reported: Z to A", (reports, reversed) ->
            reports.sort(Comparator.comparing(
                (Report r) -> r.getReported().getName(),
                String.CASE_INSENSITIVE_ORDER
            ).reversed())
        ),

        // Reason sort options
        REASON_ASC(
            "Reason: A to Z", (reports, reversed) ->
            reports.sort(Comparator.comparing(Report::getReason, String.CASE_INSENSITIVE_ORDER))
        ),
        REASON_DESC(
            "Reason: Z to A", (reports, reversed) ->
            reports.sort(Comparator.comparing(Report::getReason, String.CASE_INSENSITIVE_ORDER).reversed())
        ),

        // Updated by name sort options
        UPDATED_BY_ASC(
            "Updated By: A to Z", (reports, reversed) ->
            reports.sort(Comparator.comparing(
                (Report r) -> r.getUpdatedBy() != null ? r.getUpdatedBy().getName() : "",
                String.CASE_INSENSITIVE_ORDER
            ))
        ),
        UPDATED_BY_DESC(
            "Updated By: Z to A", (reports, reversed) ->
            reports.sort(Comparator.comparing(
                (Report r) -> r.getUpdatedBy() != null ? r.getUpdatedBy().getName() : "",
                String.CASE_INSENSITIVE_ORDER
            ).reversed())
        );

        private final String displayName;
        private final BiConsumer<List<Report>, Boolean> sorter;

        SortMethod(final String displayName, final BiConsumer<List<Report>, Boolean> sorter) {
            this.displayName = displayName;
            this.sorter = sorter;
        }

        public void sort(final List<Report> reports) {
            sorter.accept(reports, false);
        }

        public SortMethod previous() {
            final SortMethod[] values = values();
            return values[(this.ordinal() + values.length - 1) % values.length];
        }

        public SortMethod next() {
            final SortMethod[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    @Getter
    private SortMethod currentSort = SortMethod.DATE_DESC;
    private final BiConsumer<SortMethod, List<Report>> sortListener;
    private final List<Report> reports;

    public SortItem(final List<Report> reports, final BiConsumer<SortMethod, List<Report>> sortListener) {
        this.reports = reports;
        this.sortListener = sortListener;
        this.currentSort.sort(reports);
    }

    @Override
    public void handleClick(
        @NotNull final ClickType clickType,
        @NotNull final Player player,
        @NotNull final InventoryClickEvent event
    ) {
        if (clickType.isLeftClick()) {
            currentSort = currentSort.next();
            currentSort.sort(reports);
            sortListener.accept(currentSort, reports);
            notifyWindows();

            player.playSound(player.getLocation(), "ui.button.click", 1.0f, 1.0f);
        } else if (clickType.isRightClick()) {
            currentSort = currentSort.previous();
            currentSort.sort(reports);
            sortListener.accept(currentSort, reports);
            notifyWindows();

            player.playSound(player.getLocation(), "ui.button.click", 1.0f, 1.0f);
        }
    }

    @Override
    public ItemProvider getItemProvider() {
        final List<String> lore = Arrays.stream(SortMethod.values())
            .map(method -> method.getDisplayName() + (currentSort == method ? " §a✓" : ""))
            .collect(Collectors.toList());

        return new ItemBuilder(Material.LADDER)
            .setDisplayName("§6Sort Reports")
            .setLegacyLore(lore);
    }

    public void setCurrentSort(final SortMethod sortMethod) {
        this.currentSort = sortMethod;
        currentSort.sort(reports);
        notifyWindows();
    }
}
