package com.freelancer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long          id;
    private String        title;
    private String        content;
    private String        type;
    private String        referenceType;
    private Long          referenceId;
    @JsonProperty("isRead")
    private boolean       isRead;
    private LocalDateTime createdAt;
}
