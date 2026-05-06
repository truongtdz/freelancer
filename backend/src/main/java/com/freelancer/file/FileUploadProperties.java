package com.freelancer.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.upload")
@Component
@Data
public class FileUploadProperties {
    private String dir;
    private String baseUrl;
    private int maxFileSizeMb = 10;
}
