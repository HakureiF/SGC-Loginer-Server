package com.seer.seerweb.component;

import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.utils.AesUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


@Component
public class RequestInterceptor implements HandlerInterceptor {
  @Autowired
  ConfigContent configContent;
  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Override
  public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
    String token = null;
    String userid = null;
    String seerToken = request.getHeader("seer-token");
    String seerUserid = request.getHeader("seer-userid");
    if (seerToken != null) {
      token = AesUtil.decrypt(seerToken, configContent.getAesKey());
    }
    if (seerUserid != null) {
      userid = AesUtil.decrypt(seerUserid, configContent.getAesKey());
    }
    String websiteToken;
    String loginerToken;
    if (userid != null) {
      websiteToken = (String) redisTemplate.opsForHash().get(userid, "token:website");
      loginerToken = (String) redisTemplate.opsForHash().get(userid, "token:loginer");
      if ((loginerToken != null && loginerToken.equals(token)) || (websiteToken != null && websiteToken.equals(token))) {
        return true;
      }
    }
    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 设置错误状态码
    response.getWriter().write("Access denied"); // 设置错误消息
    response.sendError(401);
    return false;
  }
  @Override
  public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, @Nullable ModelAndView modelAndView) {
  }
  @Override
  public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, @Nullable Exception ex) {
  }
}
