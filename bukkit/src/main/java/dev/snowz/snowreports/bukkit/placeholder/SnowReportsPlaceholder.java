package dev.snowz.snowreports.bukkit.placeholder;

import dev.snowz.snowreports.bukkit.SnowReports;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public final class SnowReportsPlaceholder extends PlaceholderExpansion {

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", SnowReports.getInstance().getDescription().getAuthors());
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "snowreports";
    }

    @Override
    @NotNull
    public String getVersion() {
        return SnowReports.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("reports_total")) {
            try {
                return String.valueOf(SnowReports.getReportDao().countOf());
            } catch (SQLException e) {
                SnowReports.getInstance().getLogger().warning("Failed to fetch report count: " + e.getMessage());
                return "0";
            }
        }

        if (params.equalsIgnoreCase("reports_open")) {
            try {
                return String.valueOf(SnowReports.getReportDao().queryBuilder()
                    .where().eq("status", "OPEN").countOf());
            } catch (SQLException e) {
                SnowReports.getInstance().getLogger().warning("Failed to fetch open report count: " + e.getMessage());
                return "0";
            }
        }

        if (params.equalsIgnoreCase("reports_inprogress")) {
            try {
                return String.valueOf(SnowReports.getReportDao().queryBuilder()
                    .where().eq("status", "IN_PROGRESS").countOf());
            } catch (SQLException e) {
                SnowReports.getInstance().getLogger().warning("Failed to fetch in-progress report count: " + e.getMessage());
                return "0";
            }
        }

        if (params.equalsIgnoreCase("reports_closed")) {
            try {
                return String.valueOf(SnowReports.getReportDao().queryBuilder()
                    .where().eq("status", "CLOSED").countOf());
            } catch (SQLException e) {
                SnowReports.getInstance().getLogger().warning("Failed to fetch closed report count: " + e.getMessage());
                return "0";
            }
        }

        return null;
    }
}
