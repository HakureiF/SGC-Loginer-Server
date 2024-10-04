package com.seer.seerweb.config;

import com.seer.seerweb.component.DefendInterceptor;
import com.seer.seerweb.component.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Autowired
  private RequestInterceptor requestInterceptor;
  @Autowired
  private DefendInterceptor defendInterceptor;


  @Override
  public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(requestInterceptor)
          .addPathPatterns("/**")
          .excludePathPatterns(//不拦截路径
                  "/api/login-information/loginerlogin",
                  "/api/announcement/getLoginerAnnouncement",
                  "/api/game-information/removeGameCache",
                  "/api/race-group/searchGroup",
                  "/api/conventional/getMatchScoreBoard",
                  "/api/conventional/getMatchPlayers"
          );
      registry.addInterceptor(defendInterceptor)
          .addPathPatterns("/**");
  }

}
