package dev.snowz.snowreports.bukkit.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import dev.snowz.snowreports.api.event.ReportStatusUpdateEvent;
import dev.snowz.snowreports.api.model.ReportStatus;
import dev.snowz.snowreports.bukkit.SnowReports;
import dev.snowz.snowreports.bukkit.util.DiscordWebhook;
import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.common.database.entity.Report;
import dev.snowz.snowreports.common.database.entity.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static dev.snowz.snowreports.bukkit.manager.MessageManager.getMessage;

public final class ReportManager {

    public Report createReport(
        final Player reporter,
        final Player reported,
        final String reason,
        final long createdAt
    ) {
        final User reporterUser = SnowReports.getUserManager().getOrCreateUser(reporter);
        final User reportedUser = SnowReports.getUserManager().getOrCreateUser(reported);

        final Report report = new Report();
        report.setReporter(reporterUser);
        report.setReported(reportedUser);
        report.setReason(reason);
        report.setCreatedAt(createdAt);
        report.setStatus(ReportStatus.OPEN);
        report.setLastUpdated(createdAt);
        report.setServer(Config.get().getServerName());
        report.setChatHistory(SnowReports.getChatHistoryManager().getPlayerChatHistory(reported.getUniqueId()));

        // Send report to plugin channel
        final Gson gson = new Gson();
        final String reportJson = gson.toJson(report);

        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(reportJson);

        reporter.sendPluginMessage(SnowReports.getInstance(), "snowreports:main", output.toByteArray());

        try {
            SnowReports.getReportDao().create(report);
            return report;
        } catch (final SQLException e) {
            SnowReports.getInstance().getLogger().severe("Failed to create report: " + e.getMessage());
            return null;
        }
    }

    public boolean updateReportStatus(final int reportId, final ReportStatus newStatus, final Player staff) {
        final User staffUser = staff == null
            ? SnowReports.getUserManager().getOrCreateUser("00000000-0000-0000-0000-000000000000", "CONSOLE")
            : SnowReports.getUserManager().getOrCreateUser(staff);
        assert staffUser != null;

        final Report report = getReportById(reportId);
        if (report == null) {
            return false;
        }

        final ReportStatus oldStatus = report.getStatus();
        if (oldStatus == newStatus) {
            return false;
        }

        final ReportStatusUpdateEvent event = new ReportStatusUpdateEvent(
            report.toModel(),
            oldStatus,
            newStatus,
            staffUser.toModel()
        );
        SnowReports.runSync(() -> Bukkit.getPluginManager().callEvent(event));
        if (event.isCancelled()) {
            return false;
        }

        report.setStatus(event.getNewStatus());
        report.setLastUpdated(Instant.now().getEpochSecond());
        report.setUpdatedBy(User.fromModel(event.getUpdatedBy()));

        try {
            SnowReports.getReportDao().update(report);

            final Player reporter = Bukkit.getPlayer(UUID.fromString(report.getReporter().getUuid()));
            if (reporter != null && reporter.isOnline()) {
                reporter.sendMessage(getMessage(
                    "report.status_updated",
                    report.getReported().getName(),
                    newStatus.name().toLowerCase(),
                    staffUser.getName()
                ));
            }

            final String webhookURL = Config.get().getDiscord().getWebhookUrl();
            if (Config.get().getDiscord().isEnabled() && !webhookURL.isEmpty()) {
                final DiscordWebhook webhook = new DiscordWebhook(webhookURL);
                final DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                    .setTitle("Report #" + reportId + " Status Updated")
                    .setDescription(
                        "**Reporter:** `" + report.getReporter().getName() + "`" +
                            "\n**Reported:** `" + report.getReported().getName() + "`" +
                            "\n**Old Status:** " + oldStatus.getName() +
                            "\n**New Status:** " + newStatus.getName() +
                            "\n**Updated By:** " + staffUser.getName() +
                            "\n**Time:** <t:" + report.getLastUpdated() + ":f>" +
                            "\n**Server:** " + Config.get().getServerName())
                    .setFooter(
                        "Updated by: " + staffUser.getName(),
                        "https://mc-heads.net/head/" + staffUser.getUuid()
                    )
                    .setColor(Color.decode(newStatus.getColor()));

                webhook.setUsername("SnowReports");
                webhook.addEmbed(embed);
                try {
                    webhook.execute();
                } catch (final Exception e) {
                    SnowReports.getInstance().getLogger().severe("Failed to send Discord webhook: " + e.getMessage());
                }
            }

            return true;
        } catch (final SQLException e) {
            SnowReports.getInstance().getLogger().severe("Failed to update report status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReport(final int reportId) {
        try {
            return SnowReports.getReportDao().deleteById(reportId) > 0;
        } catch (final NumberFormatException e) {
            SnowReports.getInstance().getLogger().warning("Invalid report ID format: " + reportId);
            return false;
        } catch (final SQLException e) {
            SnowReports.getInstance().getLogger().warning("Failed to delete report with ID '" + reportId + "': " + e.getMessage());
            return false;
        }
    }

    public List<Integer> getReportIds() {
        try {
            return SnowReports.getReportDao().queryForAll()
                .stream()
                .map(Report::getId)
                .toList();
        } catch (final SQLException e) {
            SnowReports.getInstance().getLogger().warning("Failed to fetch report IDs: " + e.getMessage());
            return List.of();
        }
    }

    public List<Report> getReportsByStatus(final ReportStatus status) {
        try {
            return SnowReports.getReportDao().queryBuilder()
                .where().eq("status", status)
                .query();
        } catch (final SQLException e) {
            SnowReports.getInstance().getLogger().warning("Failed to fetch reports by status: " + e.getMessage());
            return List.of();
        }
    }

    public Report getReportById(final int reportId) {
        try {
            return SnowReports.getReportDao().queryForId(reportId);
        } catch (final SQLException e) {
            SnowReports.getInstance().getLogger().warning("Failed to fetch reports by ID: " + e.getMessage());
            return null;
        }
    }

    public boolean processReport(
        final Player reporter,
        final Player reported,
        final String reason
    ) {
        final long time = Instant.now().getEpochSecond();

        final Report report = createReport(reporter, reported, reason, time);
        if (report == null) {
            reporter.sendMessage(getMessage("report.creation_failed"));
            return false;
        }

        final String webhookURL = Config.get().getDiscord().getWebhookUrl();
        if (Config.get().getDiscord().isEnabled() && !webhookURL.isEmpty()) {
            final DiscordWebhook webhook = new DiscordWebhook(webhookURL);
            final DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                .setTitle(Config.get().getDiscord().getEmbed().getTitle())
                .setDescription("**Reporter:** `" + reporter.getName() + "`" +
                    "\\n**Reported:** `" + reported.getName() + "`" +
                    "\\n**Reason:** " + reason +
                    "\\n**Time:** <t:" + time + ":f>" +
                    "\\n**Server**: " + Config.get().getServerName() +
                    "\\n**Location:** " + reporter.getLocation().getBlockX() + ", " + reporter.getLocation().getBlockY() + ", " + reporter.getLocation().getBlockZ() + " (" + reporter.getWorld().getName() + ")")
                .setFooter(
                    "Reported by: " + reporter.getName(),
                    "https://mc-heads.net/head/" + reporter.getUniqueId()
                )
                .setThumbnail("https://mc-heads.net/head/" + reported.getUniqueId())
                .setColor(Color.decode(Config.get().getDiscord().getEmbed().getHexColor()));

            webhook.setUsername("SnowReports");
            webhook.addEmbed(embed);
            try {
                webhook.execute();
            } catch (final Exception e) {
                SnowReports.getInstance().getLogger().severe("Failed to send Discord webhook: " + e.getMessage());
            }
        }

        SnowReports.getAlertManager().broadcastAlert(report);

        reporter.sendMessage(getMessage("report.sent"));

        if (!reporter.hasPermission("snowreports.bypass.cooldown")) {
            SnowReports.getCooldownManager().setCooldown(
                reporter.getUniqueId(),
                Duration.ofSeconds(Config.get().getReports().getCooldown())
            );
        }

        return true;
    }
}
