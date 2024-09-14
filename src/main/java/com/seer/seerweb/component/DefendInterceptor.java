package com.seer.seerweb.component;

import com.seer.seerweb.annotation.AccessLimit;
import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.entity.AttackLog;
import com.seer.seerweb.service.AttackLogService;
import com.seer.seerweb.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Component
@Log
public class DefendInterceptor implements HandlerInterceptor {
  @Autowired
  ConfigContent configContent;
  @Autowired
  private RedisTemplate<String,String> redisTemplate;
  @Autowired
  AttackLogService attackLogService;

  @Override
  public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
    String ip = StringUtils.getIp(request);
    HandlerMethod hm = (HandlerMethod) handler;
    //获取方法中的注解,看是否有该注解
    AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
    if (accessLimit != null) {
      int seconds = accessLimit.seconds();
      int maxCount = accessLimit.maxCount();
      Integer count = (Integer) redisTemplate.opsForHash().get(ip, "SECOND_ACCESS");
      if(count == null) {
        //第一次访问
        redisTemplate.opsForHash().put(ip, "SECOND_ACCESS", 1);
        redisTemplate.expire(ip, seconds, TimeUnit.SECONDS);
      } else if(count < maxCount) {
        redisTemplate.execute(new SessionCallback<>() {
          @Override
          public Object execute(@NotNull RedisOperations operations) throws DataAccessException {
            operations.multi();
            if (Boolean.TRUE.equals(operations.hasKey(ip))) {
              //加1
              operations.opsForHash().increment(ip,"SECOND_ACCESS", 1);
            }
            return operations.exec();
          }
        });
      } else {
        //超出访问次数
        log.info("访问过快ip:" + ip + " 且在   " + seconds + " 秒内超过最大限制" + maxCount + " 次数达到    ====> " + count);
        AttackLog attackLog = new AttackLog();
        attackLog.setIp(ip);
        attackLog.setType("ip多次请求");
        attackLog.setDate(new Date());
        attackLogService.save(attackLog);
        response.getWriter().write("检测到您请求次数过多，ip已被记录");
        response.sendError(404);
        return false;
      }
    }
    return true;
  }
  @Override
  public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, @Nullable ModelAndView modelAndView) {
  }
  @Override
  public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, @Nullable Exception ex) {
  }
}
