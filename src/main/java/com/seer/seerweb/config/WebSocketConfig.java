package com.seer.seerweb.config;

import com.seer.seerweb.component.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
  /**
   * 	注入ServerEndpointExporter，
   * 	这个bean会自动注册使用了@ServerEndpoint注解声明的Websocket endpoint
   */
  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }

  @Autowired
  private LoginerWSInterceptor loginerWSInterceptor;
  @Autowired
  LoginerWS loginerWS;
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(loginerWS,"/loginer")
        .setAllowedOrigins("*")
        .addInterceptors(loginerWSInterceptor);
    registry.addHandler(loginerWS,"/loginer/sockjs").setAllowedOrigins("*").withSockJS();
  }
  @Bean
  public ServletServerContainerFactoryBean createWebSocketContainer() {
    ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

    container.setMaxTextMessageBufferSize(512000);
    container.setMaxBinaryMessageBufferSize(512000);
    container.setMaxSessionIdleTimeout(60 * 1000L);
    return container;
  }

}
