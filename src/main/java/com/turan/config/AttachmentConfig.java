package com.turan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("attachment.server")
@Data
public class AttachmentConfig
{
    private String fileBasePath;
    private int port;
    private String addr;
}
