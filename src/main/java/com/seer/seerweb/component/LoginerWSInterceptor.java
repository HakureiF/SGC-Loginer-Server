package com.seer.seerweb.component;

import com.seer.seerweb.service.UserInformationService;
import com.seer.seerweb.utils.TokenGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
@Component
@Log
public class LoginerWSInterceptor implements HandshakeInterceptor {
  @Autowired
  UserInformationService userInformationService;
  @Autowired
  TokenGenerator tokenGenerator;
  @Autowired
  RedisTemplate<String, String> redisTemplate;
  @Override
  public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {
    if (request instanceof ServletServerHttpRequest) {
      HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
      String userid = servletRequest.getParameter("userid");
      String version = servletRequest.getParameter("version");
      log.info(userid + " Try to connect");
      if (version == null || !(version.equals("1.1.4") || version.equals("1.1.5"))) {
        return false;
      }
      if (userid.startsWith("seeraccount")){
        attributes.put("userid", userid);
        if (LoginerWS.checkLoginerState(userid)){
          return true;
        }
      }
      String password = servletRequest.getParameter("password");
      // 存储到 attributes 中
      attributes.put("userid", userid);
      if (LoginerWS.checkLoginerState(userid) && userInformationService.verifyLogin(userid, password)){
        return true;
      }
    }
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    log.info("Connect fail");
    return false;
  }

  @Override
  public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, Exception exception) {

  }
}
