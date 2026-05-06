package com.freelancer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.vnpay")
public class VNPayConfig {

    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String apiUrl;
    private String returnUrl;
    private String frontendSuccessUrl;
    private String frontendCancelUrl;
    private String ipnAllowedIps;
    private int expireMinutes = 15;
}
