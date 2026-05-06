package com.freelancer.notification.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PayoutCompletedEvent extends NotificationEvent {
    private BigDecimal netAmount;
}
