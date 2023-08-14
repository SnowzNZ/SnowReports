package dev.snowz.snowreports.report;

public class Report {

    private final int reportID;
    private final String reportedPlayerUUID;
    private final String reporterUUID;
    private final String reason;
    private final String timeStamp;

    public Report(int reportID, String reportedPlayerUUID, String reporterUUID, String reason, String timeStamp) {
        this.reportID = reportID;
        this.reportedPlayerUUID = reportedPlayerUUID;
        this.reporterUUID = reporterUUID;
        this.reason = reason;
        this.timeStamp = timeStamp;
    }

    public int getReportID() {
        return reportID;
    }

    public String getReportedPlayerUUID() {
        return reportedPlayerUUID;
    }

    public String getReporterUUID() {
        return reporterUUID;
    }

    public String getReason() {
        return reason;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}

