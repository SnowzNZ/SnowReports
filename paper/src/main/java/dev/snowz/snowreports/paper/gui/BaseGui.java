package dev.snowz.snowreports.paper.gui;

import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;

/**
 * SnowReports GUI
 *
 * @param <T> The type of GUI that will be created
 */
public interface BaseGui<T extends Gui> {

    /**
     * Creates and returns the GUI instance.
     *
     * @return The created GUI
     */
    @NonNull
    T create();
}
