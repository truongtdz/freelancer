package com.freelancer.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PaymentReceivedEvent extends NotificationEvent {
    private String     jobTitle;
    private BigDecimal amount;
}
