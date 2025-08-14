package dev.snowz.snowreports.paper.gui.impl;

import dev.snowz.snowreports.api.model.ChatMessage;
import dev.snowz.snowreports.paper.SnowReports;
import dev.snowz.snowreports.paper.gui.BaseGui;
import dev.snowz.snowreports.paper.gui.item.BackItem;
import dev.snowz.snowreports.paper.gui.item.ChatHistoryItem;
import dev.snowz.snowreports.paper.gui.item.NextPageItem;
import dev.snowz.snowreports.paper.gui.item.PreviousPageItem;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.*;

public final class ChatHistoryGui implements BaseGui<PagedGui<Item>> {
    private static final Map<UUID, Window> activeWindows = new HashMap<>();

    private final int id;
    private final ChatMessage[] chatHistory;

    public ChatHistoryGui(final int id) {
        this.id = id;
        this.chatHistory = Objects.requireNonNull(SnowReports.getReportManager().getReportById(id)).getChatHistory();
    }

    @Override
    public @NonNull PagedGui<Item> create() {
        final List<Item> chatHistoryItems = new ArrayList<>();
        for (final ChatMessage chatMessage : chatHistory) {
            chatHistoryItems.add(new ChatHistoryItem(chatMessage));
        }

        return PagedGui.items()
            .setStructure(
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "# x x x x x x x #",
                "- < # # * # # > #"
            )
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('-', new BackItem())
            .addIngredient('<', new PreviousPageItem())
            .addIngredient('>', new NextPageItem())
            .setContent(chatHistoryItems)
            .build();
    }

    /**
     * Opens the reports GUI for a player
     *
     * @param player The player to open the GUI for
     */
    public void open(final Player player) {
        final PagedGui<Item> gui = create();
        final Window window = Window.single()
            .setViewer(player)
            .setGui(gui)
            .setTitle("Chat History")
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
