package dev.snowz.snowreports;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private final SnowReports plugin = SnowReports.getPlugin();
    private Connection connection;

    public Database() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + File.separator + "data.db");

            plugin.getLogger().info("Connected to database.");

            connection.prepareStatement("CREATE TABLE IF NOT EXISTS Reports (reportID INTEGER PRIMARY KEY AUTOINCREMENT, reportedPlayerUUID TEXT, reporterUUID TEXT, reason TEXT, timeStamp TEXT)").execute();

        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public void safeDisconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Disconnected from database");
            } catch (SQLException e) {
                plugin.getLogger().severe(e.getMessage());
            }
        }
    }

    public boolean isClosed() {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    public void insertReport(String reportedPlayerUUID, String reporterUUID, String reason, String timeStamp) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Reports (reportedPlayerUUID, reporterUUID, reason, timeStamp) VALUES ( ?, ?, ?, ?)");
            statement.setString(1, reportedPlayerUUID);
            statement.setString(2, reporterUUID);
            statement.setString(3, reason);
            statement.setString(4, timeStamp);
            statement.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public List<Report> getPlayerReports(String reportedPlayerUUID) {
        List<Report> playerReports = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT reportID, reporterUUID, reason, timeStamp FROM Reports WHERE reportedPlayerUUID = ?");
            statement.setString(1, reportedPlayerUUID);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    int reportID = results.getInt("reportID");
                    String reporterUUID = results.getString("reporterUUID");
                    String reason = results.getString("reason");
                    String timeStamp = results.getString("timeStamp");

                    playerReports.add(new Report(reportID, reportedPlayerUUID, reporterUUID, reason, timeStamp));
                }
                return playerReports;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }

        return null;
    }

    public List<Report> getAllReports() {
        List<Report> allReports = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT reportID, reportedPlayerUUID, reporterUUID, reason, timeStamp FROM Reports");

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    int reportID = results.getInt("reportID");
                    String reportedPlayerUUID = results.getString("reportedPlayerUUID");
                    String reporterUUID = results.getString("reporterUUID");
                    String reason = results.getString("reason");
                    String timeStamp = results.getString("timeStamp");

                    allReports.add(new Report(reportID, reportedPlayerUUID, reporterUUID, reason, timeStamp));
                }
                return allReports;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }

        return null;
    }

    public List<String> getAllReportIDs() {
        List<String> reportIDs = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT reportID FROM Reports");

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    int reportID = results.getInt("reportID");
                    reportIDs.add(Integer.toString(reportID));
                }
                return reportIDs;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }

        return null;
    }


    public boolean deleteReport(int reportID) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM Reports WHERE reportID = ?");
            statement.setInt(1, reportID);
            int deleted = statement.executeUpdate();

            return deleted != 0;
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }

        return false;
    }
}
