package dev.snowz.snowreports.paper.gui.impl;

import dev.snowz.snowreports.paper.gui.BaseGui;
import dev.snowz.snowreports.paper.gui.item.BackItem;
import dev.snowz.snowreports.paper.gui.item.InProgressItem;
import dev.snowz.snowreports.paper.gui.item.OpenItem;
import dev.snowz.snowreports.paper.gui.item.ResolveItem;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class UpdateStatusGui implements BaseGui<Gui> {
    private static final Map<UUID, Window> activeWindows = new HashMap<>();

    private final int id;

    public UpdateStatusGui(final int id) {
        this.id = id;
    }

    @Override
    public @NonNull Gui create() {
        return Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# # 1 # 2 # 3 # #",
                "- # # # # # # # #"
            )
            .addIngredient('1', new OpenItem(id))
            .addIngredient('2', new InProgressItem(id))
            .addIngredient('3', new ResolveItem(id))
            .addIngredient('-', new BackItem())
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
            .setTitle("Update Report Status")
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
