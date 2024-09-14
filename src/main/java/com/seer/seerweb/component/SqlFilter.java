package com.seer.seerweb.component;

import com.seer.seerweb.service.AttackLogService;
import com.seer.seerweb.utils.StringUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * The type Sql filter.
 */
@Configuration
@WebFilter(urlPatterns = "/*", filterName = "sqlFilter")
public class SqlFilter implements Filter {
  /**
   * The Attack log service.
   */
  @Autowired
  AttackLogService attackLogService;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Filter.super.init(filterConfig);
  }

  @Override
  public void doFilter(ServletRequest servletRequest,
                       ServletResponse servletResponse,
                       FilterChain filterChain)
      throws IOException, ServletException {
    String ip = StringUtils.getIp((HttpServletRequest) servletRequest);
    //获得所有请求参数名
    Enumeration<String> names = servletRequest.getParameterNames();
    StringBuilder sql = new StringBuilder();
    while (names.hasMoreElements()) {
      //得到参数名
      String name = names.nextElement();
      //得到参数对应值
      String[] values = servletRequest.getParameterValues(name);
      for (String value : values) {
        sql.append(value);
      }
    }
    if (attackLogService.sqlCheck(sql.toString(), ip)) {
      throw new IOException("您发送请求中的参数中含有非法字符");
    } else {
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }

  @Override
  public void destroy() {
    Filter.super.destroy();
  }
}
