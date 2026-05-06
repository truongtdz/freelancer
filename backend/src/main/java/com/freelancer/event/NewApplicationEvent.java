package com.freelancer.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class NewApplicationEvent extends NotificationEvent {
    private String jobTitle;
    private String freelancerName;
}
