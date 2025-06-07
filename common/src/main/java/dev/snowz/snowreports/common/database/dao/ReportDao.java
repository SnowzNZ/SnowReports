package dev.snowz.snowreports.common.database.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import dev.snowz.snowreports.common.database.entity.Report;

import java.sql.SQLException;
import java.util.List;

public final class ReportDao extends BaseDaoImpl<Report, Integer> {

    public ReportDao(final ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Report.class);
    }

    /**
     * Find reports by reporter UUID
     */
    public List<Report> findByReporter(final String reporterUuid) throws SQLException {
        return queryBuilder()
            .where()
            .eq("reporter_uuid", reporterUuid)
            .query();
    }

    /**
     * Find reports against a specific user
     */
    public List<Report> findByReported(final String reportedUuid) throws SQLException {
        return queryBuilder()
            .where()
            .eq("reported_uuid", reportedUuid)
            .query();
    }

    /**
     * Find reports within a time range
     */
    public List<Report> findByTimeRange(final long startTime, final long endTime) throws SQLException {
        return queryBuilder()
            .where()
            .between("time", startTime, endTime)
            .query();
    }

    /**
     * Find reports by server
     */
    public List<Report> findByServer(final String server) throws SQLException {
        return queryBuilder()
            .where()
            .eq("server", server)
            .query();
    }

    /**
     * Get recent reports (last N reports)
     */
    public List<Report> getRecentReports(final int limit) throws SQLException {
        return queryBuilder()
            .orderBy("time", false)
            .limit((long) limit)
            .query();
    }

    /**
     * Count reports for a specific user
     */
    public long countReportsAgainst(final String reportedUuid) throws SQLException {
        return queryBuilder()
            .where()
            .eq("reported_uuid", reportedUuid)
            .countOf();
    }
}
