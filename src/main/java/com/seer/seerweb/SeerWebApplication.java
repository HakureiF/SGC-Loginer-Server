package com.seer.seerweb;

import com.github.jeffreyning.mybatisplus.conf.EnableMPP;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@MapperScan("com.seer.seerweb.mapper")
@ConfigurationPropertiesScan
@EnableWebMvc
@EnableMPP
@EnableScheduling
@ServletComponentScan("com.seer.seerweb.component.SQLFilter")
public class SeerWebApplication {
  public static void main(String[] args) {
    SpringApplication.run(SeerWebApplication.class, args);
  }

}
