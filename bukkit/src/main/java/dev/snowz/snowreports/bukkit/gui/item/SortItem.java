package dev.snowz.snowreports.bukkit.gui.item;

import dev.snowz.snowreports.common.database.entity.Report;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class SortItem extends AbstractItem {

    @Getter
    public enum SortCategory {
        DATE("Date"),
        REPORTER("Reporter"),
        REPORTED("Reported"),
        REASON("Reason"),
        UPDATED_BY("Updated By"),
        PLAYER_STATUS("Player Status");

        private final String displayName;

        SortCategory(final String displayName) {
            this.displayName = displayName;
        }

        public SortCategory next() {
            final SortCategory[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }

        public SortCategory previous() {
            final SortCategory[] values = values();
            return values[(this.ordinal() + values.length - 1) % values.length];
        }
    }

    @Getter
    public enum SortMethod {
        // Date sort options
        DATE_ASC(
            "Oldest to Newest", SortCategory.DATE, (reports, reversed) ->
            reports.sort(Comparator.comparing(Report::getCreatedAt))
        ),
        DATE_DESC(
            "Newest to Oldest", SortCategory.DATE, (reports, reversed) ->
            reports.sort(Comparator.comparing(Report::getCreatedAt).reversed())
        ),

        // Reporter name sort options
        REPORTER_NAME_ASC(
            "A to Z", SortCategory.REPORTER, (reports, reversed) ->
            reports.sort(Comparator.comparing((Report r) -> r.getReporter().getName(), String.CASE_INSENSITIVE_ORDER))
        ),
        REPORTER_NAME_DESC(
            "Z to A", SortCategory.REPORTER, (reports, reversed) ->
            reports.sort(Comparator.comparing(
                (Report r) -> r.getReporter().getName(),
                String.CASE_INSENSITIVE_ORDER
            ).reversed())
        ),

        // Reported name sort options
        REPORTED_NAME_ASC(
            "A to Z", SortCategory.REPORTED, (reports, reversed) ->
            reports.sort(Comparator.comparing((Report r) -> r.getReported().getName(), String.CASE_INSENSITIVE_ORDER))
        ),
        REPORTED_NAME_DESC(
            "Z to A", SortCategory.REPORTED, (reports, reversed) ->
            reports.sort(Comparator.comparing(
                (Report r) -> r.getReported().getName(),
                String.CASE_INSENSITIVE_ORDER
            ).reversed())
        ),

        // Reason sort options
        REASON_ASC(
            "A to Z", SortCategory.REASON, (reports, reversed) ->
            reports.sort(Comparator.comparing(Report::getReason, String.CASE_INSENSITIVE_ORDER))
        ),
        REASON_DESC(
            "Z to A", SortCategory.REASON, (reports, reversed) ->
            reports.sort(Comparator.comparing(Report::getReason, String.CASE_INSENSITIVE_ORDER).reversed())
        ),

        // Updated by name sort options
        UPDATED_BY_ASC(
            "A to Z", SortCategory.UPDATED_BY, (reports, reversed) ->
            reports.sort(Comparator.comparing(
                (Report r) -> r.getUpdatedBy() != null ? r.getUpdatedBy().getName() : "",
                String.CASE_INSENSITIVE_ORDER
            ))
        ),
        UPDATED_BY_DESC(
            "Z to A", SortCategory.UPDATED_BY, (reports, reversed) ->
            reports.sort(Comparator.comparing(
                (Report r) -> r.getUpdatedBy() != null ? r.getUpdatedBy().getName() : "",
                String.CASE_INSENSITIVE_ORDER
            ).reversed())
        ),

        PLAYER_STATUS_ONLINE_FIRST(
            "Online First", SortCategory.PLAYER_STATUS, (reports, reversed) -> {
            Map<String, Boolean> onlineCache = reports.stream()
                .collect(Collectors.toMap(
                    r -> r.getReported().getUuid(),
                    r -> {
                        Player player = Bukkit.getPlayer(UUID.fromString(r.getReported().getUuid()));
                        return player != null && player.isOnline();
                    },
                    (existing, replacement) -> existing // Handle duplicates
                ));

            reports.sort(Comparator.comparing((Report r) ->
                onlineCache.get(r.getReported().getUuid())).reversed());
        }
        ),
        PLAYER_STATUS_OFFLINE_FIRST(
            "Offline First", SortCategory.PLAYER_STATUS, (reports, reversed) -> {
            Map<String, Boolean> onlineCache = reports.stream()
                .collect(Collectors.toMap(
                    r -> r.getReported().getUuid(),
                    r -> {
                        Player player = Bukkit.getPlayer(UUID.fromString(r.getReported().getUuid()));
                        return player != null && player.isOnline();
                    },
                    (existing, replacement) -> existing
                ));

            reports.sort(Comparator.comparing((Report r) ->
                onlineCache.get(r.getReported().getUuid())));
        }
        );

        private final String displayName;
        private final SortCategory category;
        private final BiConsumer<List<Report>, Boolean> sorter;

        SortMethod(
            final String displayName,
            final SortCategory category,
            final BiConsumer<List<Report>, Boolean> sorter
        ) {
            this.displayName = displayName;
            this.category = category;
            this.sorter = sorter;
        }

        public void sort(final List<Report> reports) {
            sorter.accept(reports, false);
        }

        public static List<SortMethod> getByCategory(final SortCategory category) {
            final List<SortMethod> methods = new ArrayList<>();
            for (final SortMethod method : values()) {
                if (method.category == category) {
                    methods.add(method);
                }
            }
            return methods;
        }

        public SortMethod nextInCategory() {
            final List<SortMethod> methodsInCategory = getByCategory(this.category);
            final int currentIndex = methodsInCategory.indexOf(this);
            return methodsInCategory.get((currentIndex + 1) % methodsInCategory.size());
        }

        public SortMethod previousInCategory() {
            final List<SortMethod> methodsInCategory = getByCategory(this.category);
            final int currentIndex = methodsInCategory.indexOf(this);
            return methodsInCategory.get((currentIndex + methodsInCategory.size() - 1) % methodsInCategory.size());
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
        if (clickType.isRightClick()) {
            final SortCategory nextCategory = currentSort.getCategory().next();
            currentSort = SortMethod.getByCategory(nextCategory).get(0);
            updateSort(player);
        } else if (clickType.isLeftClick()) {
            currentSort = currentSort.nextInCategory();
            updateSort(player);
        } else if (clickType.isShiftClick()) {
            currentSort = currentSort.previousInCategory();
            updateSort(player);
        }
    }

    private void updateSort(final Player player) {
        currentSort.sort(reports);
        sortListener.accept(currentSort, reports);
        notifyWindows();
        player.playSound(player.getLocation(), "ui.button.click", 1.0f, 1.0f);
    }

    @Override
    public ItemProvider getItemProvider() {
        final List<String> lore = new ArrayList<>();

        lore.add("§fCategory: §6" + currentSort.getCategory().getDisplayName());
        lore.add("");

        lore.add("§fSort Options:");
        for (final SortMethod method : SortMethod.getByCategory(currentSort.getCategory())) {
            lore.add((method == currentSort ? "§a\uD83E\uDC1A " : "§7  ") + method.getDisplayName());
        }
        lore.add("");
        lore.add("§6Right-click §fto change category.");
        lore.add("");
        lore.add("§6Left-click §fto go down.");
        lore.add("§6Shift-click §fto go up.");

        return new ItemBuilder(Material.COMPASS)
            .setDisplayName("§6Sort Reports")
            .setLegacyLore(lore);
    }

    public void setCurrentSort(final SortMethod sortMethod) {
        this.currentSort = sortMethod;
        currentSort.sort(reports);
        notifyWindows();
    }
}
