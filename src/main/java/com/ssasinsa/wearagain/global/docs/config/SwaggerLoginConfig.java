package com.ssasinsa.wearagain.global.docs.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "dev"})
@EnableConfigurationProperties(SwaggerTestLoginProperties.class)
public class SwaggerLoginConfig {
}
