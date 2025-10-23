package dev.snowz.snowreports.paper.placeholder;

import dev.snowz.snowreports.api.model.ReportStatus;
import dev.snowz.snowreports.paper.SnowReports;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public final class SnowReportsPlaceholder extends PlaceholderExpansion {

    @Override
    @NonNull
    public String getAuthor() {
        return String.join(", ", SnowReports.getInstance().getPluginMeta().getAuthors());
    }

    @Override
    @NonNull
    public String getIdentifier() {
        return "snowreports";
    }

    @Override
    @NonNull
    public String getVersion() {
        return SnowReports.VERSION;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(final Player player, @NonNull final String params) {
        if (params.equalsIgnoreCase("reports_total")) {
            try {
                return String.valueOf(SnowReports.getReportDao().countOf());
            } catch (final SQLException e) {
                SnowReports.getInstance().getLogger().warning("Failed to fetch report count: " + e.getMessage());
                return "0";
            }
        }

        if (params.equalsIgnoreCase("reports_open")) {
            try {
                return String.valueOf(SnowReports.getReportDao().queryBuilder()
                    .where()
                    .eq("status", ReportStatus.OPEN).countOf());
            } catch (final SQLException e) {
                SnowReports.getInstance().getLogger().warning("Failed to fetch open report count: " + e.getMessage());
                return "0";
            }
        }

        if (params.equalsIgnoreCase("reports_inprogress")) {
            try {
                return String.valueOf(SnowReports.getReportDao().queryBuilder()
                    .where()
                    .eq("status", ReportStatus.IN_PROGRESS).countOf());
            } catch (final SQLException e) {
                SnowReports.getInstance().getLogger().warning("Failed to fetch in-progress report count: " + e.getMessage());
                return "0";
            }
        }

        if (params.equalsIgnoreCase("reports_resolved")) {
            try {
                return String.valueOf(SnowReports.getReportDao().queryBuilder()
                    .where()
                    .eq("status", ReportStatus.RESOLVED)
                    .countOf());
            } catch (final SQLException e) {
                SnowReports.getInstance().getLogger().warning("Failed to fetch resolved report count: " + e.getMessage());
                return "0";
            }
        }

        return null;
    }
}
