package dev.snowz.snowreports.api.model;

public record ReportModel(
    int id,
    UserModel reporter,
    UserModel reported,
    String reason,
    long createdAt,
    ReportStatus status,
    long lastUpdated,
    UserModel updatedBy,
    String server,
    ChatMessage[] chatHistory
) {
}
