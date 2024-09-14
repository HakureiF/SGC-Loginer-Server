package com.seer.seerweb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The type Config content.
 */
@ConfigurationProperties(prefix = "config")
@Configuration
@Data
public class ConfigContent {
  private String aesKey;
  private int gameTime;
  private int tokenTime;
}
