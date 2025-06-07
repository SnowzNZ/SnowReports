package dev.snowz.snowreports.bukkit.gui.impl;

import dev.snowz.snowreports.bukkit.gui.BaseGui;
import dev.snowz.snowreports.bukkit.gui.item.CancelItem;
import dev.snowz.snowreports.bukkit.gui.item.ConfirmItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DeleteReportConfirmationGui implements BaseGui<Gui> {
    private static final Map<UUID, Window> activeWindows = new HashMap<>();

    private final int id;

    public DeleteReportConfirmationGui(final int id) {
        this.id = id;
    }

    @Override
    public @NotNull Gui create() {
        return Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# # < # # # > # #",
                "# # # # # # # # #"
            )
            .addIngredient('<', new ConfirmItem(id))
            .addIngredient('>', new CancelItem())
            .build();
    }

    /**
     * Opens the reports GUI for a player
     *
     * @param player The player to open the GUI for
     */
    public void open(final Player player) {
        final Gui gui = create();
        final Window window = Window.single()
            .setViewer(player)
            .setGui(gui)
            .setTitle("Delete Report #" + id + "?")
            .addCloseHandler(() -> activeWindows.remove(player.getUniqueId()))
            .build();

        // Store reference to allow cleanup
        activeWindows.put(player.getUniqueId(), window);

        // Open the window
        window.open();
    }

    /**
     * Force closes all active report GUI windows
     */
    public static void closeAll() {
        for (final Window window : activeWindows.values()) {
            window.close();
        }
        activeWindows.clear();
    }
}
