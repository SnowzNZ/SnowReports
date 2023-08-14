package dev.snowz.snowreports.utils;

import dev.snowz.snowreports.SnowReports;
import dev.snowz.snowreports.report.Report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportManager {

    public int getNextReportID() {
        Connection connection = SnowReports.getConnection();
        int nextReportID = 1;

        String databaseQuery = "SELECT MAX(reportID) AS maxReportID FROM Reports";

        try (PreparedStatement preparedStatement = connection.prepareStatement(databaseQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                nextReportID = resultSet.getInt("maxReportID") + 1;
            }
        } catch (SQLException e) {
            SnowReports.getInstance().getLogger().severe("Error retrieving next report ID: " + e.getMessage());
        }

        return nextReportID;
    }

    public void createReport(String reportedPlayerUUID, String reporterUUID, String reason, String timeStamp) {
        int reportID = getNextReportID();

        Connection connection = SnowReports.getConnection();

        String sql = "INSERT INTO Reports (reportID, reportedPlayerUUID, reporterUUID, reason, timeStamp) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, reportID);
            preparedStatement.setString(2, reportedPlayerUUID);
            preparedStatement.setString(3, reporterUUID);
            preparedStatement.setString(4, reason);
            preparedStatement.setString(5, timeStamp);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SnowReports.getInstance().getLogger().severe("Error inserting report into the database: " + e.getMessage());
        }
    }

    public List<Report> getReports(String reportedPlayerUUID) {
        Connection connection = SnowReports.getConnection();
        List<Report> reports = new ArrayList<>();

        String databaseQuery = "SELECT reportID, reporterUUID, reason, timeStamp FROM Reports WHERE reportedPlayerUUID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(databaseQuery)) {
            preparedStatement.setString(1, reportedPlayerUUID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int reportID = resultSet.getInt("reportID");
                    String reporterUUID = resultSet.getString("reporterUUID");
                    String reason = resultSet.getString("reason");
                    String timeStamp = resultSet.getString("timeStamp");

                    reports.add(new Report(reportID, reportedPlayerUUID, reporterUUID, reason, timeStamp));
                }
            }
        } catch (SQLException e) {
            SnowReports.getInstance().getLogger().severe("Error retrieving reports for reportedPlayerUUID: " + reportedPlayerUUID + " - " + e.getMessage());
        }

        return reports;
    }

    public void deleteReport(int reportID) {
        Connection connection = SnowReports.getConnection();

        String sql = "DELETE FROM Reports WHERE reportID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, reportID);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            SnowReports.getInstance().getLogger().severe("Error deleting report with reportID " + reportID + ": " + e.getMessage());
        }
    }

    public boolean doesReportIDExist(int reportID) {
        Connection connection = SnowReports.getConnection();

        String sql = "SELECT COUNT(*) AS count FROM Reports WHERE reportID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, reportID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            SnowReports.getInstance().getLogger().severe("Error checking if reportID exists: " + e.getMessage());
        }

        return false;
    }
}
