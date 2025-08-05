package dev.snowz.snowreports.bukkit;

import com.j256.ormlite.dao.Dao;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPILogger;
import dev.snowz.snowreports.bukkit.command.Command;
import dev.snowz.snowreports.bukkit.command.impl.*;
import dev.snowz.snowreports.bukkit.command.impl.snowreports.SnowReportsCommand;
import dev.snowz.snowreports.bukkit.listener.PlayerChatListener;
import dev.snowz.snowreports.bukkit.listener.PlayerJoinListener;
import dev.snowz.snowreports.bukkit.listener.ReportBridgeListener;
import dev.snowz.snowreports.bukkit.manager.*;
import dev.snowz.snowreports.bukkit.placeholder.SnowReportsPlaceholder;
import dev.snowz.snowreports.bukkit.util.UpdateChecker;
import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.common.database.DatabaseManager;
import dev.snowz.snowreports.common.database.entity.Report;
import dev.snowz.snowreports.common.database.entity.User;
import dev.snowz.snowreports.common.discord.DiscordWebhook;
import dev.snowz.snowreports.common.manager.ChatHistoryManager;
import lombok.Getter;
import net.byteflux.libby.BukkitLibraryManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public final class SnowReports extends JavaPlugin {

    @Getter
    private static SnowReports instance;

    @Getter
    private static DatabaseManager dbManager;

    @Getter
    private static Dao<Report, Integer> reportDao;

    @Getter
    private static Dao<User, String> userDao;

    @Getter
    private static AlertManager alertManager;

    @Getter
    private static CooldownManager cooldownManager;

    @Getter
    private static ReportManager reportManager;

    @Getter
    private static UserManager userManager;

    @Getter
    private static ChatHistoryManager chatHistoryManager;

    @Getter
    private static List<Command> commands;

    public static String VERSION;

    @SuppressWarnings("deprecation")
    private static String getVersion() {
        return instance.getDescription().getVersion();
    }

    @Getter
    private static boolean updateAvailable;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
            .beLenientForMinorVersions(true)
        );

        // Set custom CommandAPI Logger
        final Logger commandAPILogger = Logger.getLogger("SnowReports-CommandAPI");
        CommandAPI.setLogger(CommandAPILogger.fromJavaLogger(commandAPILogger));

        // Disable ORMLite logging
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(com.j256.ormlite.logger.Level.OFF);
    }

    @Override
    public void onEnable() {
        instance = this;

        VERSION = getVersion();

        // Config
        Config.init(getDataFolder());

        // Update Checker
        if (Config.get().isCheckForUpdates()) {
            updateAvailable = UpdateChecker.checkForUpdates(VERSION);
        }

        // Database
        dbManager = new DatabaseManager(getDataFolder(), new BukkitLibraryManager(this));
        try {
            dbManager.connect(Config.get());
            dbManager.createTables(Report.class, User.class);

            reportDao = dbManager.getDao(Report.class);
            userDao = dbManager.getDao(User.class);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

        // Managers
        alertManager = new AlertManager();
        cooldownManager = new CooldownManager();
        reportManager = new ReportManager();
        userManager = new UserManager();
        chatHistoryManager = new ChatHistoryManager(
            Config.get().getReports().getChatHistory().getMaxMessages(),
            Config.get().getReports().getChatHistory().getMaxAgeSeconds()
        );

        // Messages
        MessageManager.loadMessages();

        Structure.addGlobalIngredient('#', new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(""));

        // Metrics
        if (Config.get().isMetrics()) {
            new Metrics(this, 19543);
        }

        // Commands
        CommandAPI.onEnable();
        commands = List.of(
            new SnowReportsCommand(),
            new DeleteAllReportsCommand(),
            new DeleteAllReportsFromCommand(),
            new DeleteReportCommand(),
            new MyReportsCommand(),
            new ReportCommand(),
            new ReportsCommand(),
            new SetStatusCommand()
        );
        commands.forEach(Command::register);

        // Listeners
        List.of(
                new PlayerChatListener(),
                new PlayerJoinListener()
            )
            .forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        // Plugin channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "snowreports:main");
        this.getServer().getMessenger().registerIncomingPluginChannel(
            this,
            "snowreports:main",
            new ReportBridgeListener()
        );

        // Placeholders
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SnowReportsPlaceholder().register();
        }
    }

    @Override
    public void onDisable() {
        // Shutdown executors
        PlayerChatListener.shutdown();
        DiscordWebhook.shutdown();

        // Commands
        CommandAPI.onDisable();

        // Database
        dbManager.close();
    }

    public static void runAsync(final Runnable runnable) {
        getInstance().getServer().getScheduler().runTaskAsynchronously(getInstance(), runnable);
    }

    public static void runSync(final Runnable runnable) {
        getInstance().getServer().getScheduler().runTask(getInstance(), runnable);
    }
}
