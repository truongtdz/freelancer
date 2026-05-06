package com.freelancer.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SystemEvent extends NotificationEvent {
    private String title;
    private String content;
}
