package dev.snowz.snowreports.api.event;

import dev.snowz.snowreports.api.model.ReportModel;
import dev.snowz.snowreports.api.model.ReportStatus;
import dev.snowz.snowreports.api.model.UserModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public final class ReportStatusUpdateEvent extends Event {
    @Getter
    public final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final ReportModel report;
    private final ReportStatus oldStatus;
    private final ReportStatus newStatus;
    private final UserModel updatedBy;
}
