package com.freelancer.notification.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ApplicationAcceptedEvent extends NotificationEvent {
    private String jobTitle;
}
