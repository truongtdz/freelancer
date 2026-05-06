package com.freelancer.notification.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NewApplicationEvent.class,       name = "NEW_APPLICATION"),
        @JsonSubTypes.Type(value = ApplicationAcceptedEvent.class,  name = "APPLICATION_ACCEPTED"),
        @JsonSubTypes.Type(value = PaymentReceivedEvent.class,      name = "PAYMENT_RECEIVED"),
        @JsonSubTypes.Type(value = ProgressReportEvent.class,       name = "PROGRESS_REPORT"),
        @JsonSubTypes.Type(value = CompletionSubmittedEvent.class,  name = "COMPLETION_SUBMITTED"),
        @JsonSubTypes.Type(value = CompletionConfirmedEvent.class,  name = "JOB_COMPLETED"),
        @JsonSubTypes.Type(value = PayoutCompletedEvent.class,      name = "PAYOUT_COMPLETED"),
        @JsonSubTypes.Type(value = SystemEvent.class,               name = "SYSTEM"),
})
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class NotificationEvent {
    private Long          recipientUserId;
    private String        referenceType;
    private Long          referenceId;
    private LocalDateTime occurredAt;
}
