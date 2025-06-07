package dev.snowz.snowreports.api.model;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;

@Getter
public enum ReportStatus {
    OPEN("Open", Material.LIME_STAINED_GLASS_PANE, "#55FF55"),
    IN_PROGRESS("In Progress", Material.ORANGE_STAINED_GLASS_PANE, "#FFAA00"),
    RESOLVED("Resolved", Material.PURPLE_STAINED_GLASS_PANE, "#AA00AA");

    private final String name;
    private final Material material;
    private final String color;

    ReportStatus(final String name, final Material material, final String color) {
        this.name = name;
        this.material = material;
        this.color = color;
    }

    public String getDisplayName() {
        return LegacyComponentSerializer.legacySection().serialize(Component.text(name).color(TextColor.fromHexString(
            color)));
    }
}
