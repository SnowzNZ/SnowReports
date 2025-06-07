package dev.snowz.snowreports.common.database.entity;

import com.google.gson.Gson;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.snowz.snowreports.api.model.ChatMessage;
import dev.snowz.snowreports.api.model.ReportModel;
import dev.snowz.snowreports.api.model.ReportStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@DatabaseTable(tableName = "reports")
public final class Report {
    @DatabaseField(
        generatedId = true,
        columnName = "id"
    )
    private int id;

    @DatabaseField(
        foreign = true,
        columnName = "reporter_uuid",
        canBeNull = false,
        foreignAutoRefresh = true,
        index = true
    )
    private User reporter;

    @DatabaseField(
        foreign = true,
        columnName = "reported_uuid",
        canBeNull = false,
        foreignAutoRefresh = true,
        index = true
    )
    private User reported;

    @DatabaseField(
        columnName = "reason",
        canBeNull = false,
        dataType = DataType.LONG_STRING
    )
    private String reason;

    @DatabaseField(
        columnName = "created_at",
        canBeNull = false
    )
    private long createdAt;

    @DatabaseField(
        columnName = "status",
        canBeNull = false,
        index = true
    )
    private ReportStatus status = ReportStatus.OPEN;

    @DatabaseField(
        columnName = "last_updated",
        canBeNull = false
    )
    private long lastUpdated;

    @DatabaseField(
        foreign = true,
        columnName = "updated_by",
        foreignAutoRefresh = true,
        index = true
    )
    private User updatedBy;

    @DatabaseField(
        columnName = "server",
        width = 16
    )
    private String server;

    @DatabaseField(
        columnName = "chat_history",
        dataType = DataType.LONG_STRING
    )
    private String chatHistory;

    public ChatMessage[] getChatHistory() {
        if (chatHistory == null || chatHistory.isEmpty()) {
            return new ChatMessage[0];
        }
        try {
            return new Gson().fromJson(chatHistory, ChatMessage[].class);
        } catch (final Exception e) {
            return new ChatMessage[0];
        }
    }

    public void setChatHistory(final ChatMessage[] chatHistory) {
        if (chatHistory == null) {
            this.chatHistory = null;
        } else {
            this.chatHistory = new Gson().toJson(chatHistory);
        }
    }

    public Report() {
    }

    public Report(
        final User reporter,
        final User reported,
        final String reason,
        final long createdAt,
        final String server,
        final String chatHistory
    ) {
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.createdAt = createdAt;
        this.lastUpdated = createdAt;
        this.server = server;
        this.chatHistory = chatHistory;
    }

    public ReportModel toModel() {
        return new ReportModel(
            id,
            reporter != null ? reporter.toModel() : null,
            reported != null ? reported.toModel() : null,
            reason,
            createdAt,
            status,
            lastUpdated,
            updatedBy != null ? updatedBy.toModel() : null,
            server,
            getChatHistory()
        );
    }

    /**
     * Converts a ReportModel to a Report entity
     *
     * @param model The ReportModel to convert
     * @return The converted Report entity
     */
    public static Report fromModel(final ReportModel model) {
        final Report report = new Report();
        report.setId(model.id());

        // Convert reporter
        if (model.reporter() != null) {
            final User reporter = User.fromModel(model.reporter());
            report.setReporter(reporter);
        }

        // Convert reported
        if (model.reported() != null) {
            final User reported = User.fromModel(model.reported());
            report.setReported(reported);
        }

        report.setReason(model.reason());
        report.setCreatedAt(model.createdAt());
        report.setStatus(model.status());
        report.setLastUpdated(model.lastUpdated());

        // Convert updatedBy
        if (model.updatedBy() != null) {
            final User updatedBy = User.fromModel(model.updatedBy());
            report.setUpdatedBy(updatedBy);
        }

        report.setServer(model.server());
        report.setChatHistory(model.chatHistory());

        return report;
    }

    @Override
    public String toString() {
        return "Report{id="
            + id
            + ", reporter="
            + (reporter != null ? reporter.getUuid() : null)
            + ", reported="
            + (reported != null ? reported.getUuid() : null)
            + ", reason='"
            + reason
            + "', createdAt="
            + createdAt
            + ", status="
            + status
            + ", lastUpdated="
            + lastUpdated
            + ", updatedBy="
            + (updatedBy != null ? updatedBy.getUuid() : null)
            + ", server='"
            + server
            + "', chatHistory="
            + (chatHistory != null ? chatHistory : "null")
            + "'}";
    }
}
