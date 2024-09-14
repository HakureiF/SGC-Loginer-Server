package com.seer.seerweb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Access limit.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLimit {
  /**
   * 在一定时间内.
   *
   * @return the int
   */
  int seconds();

  /**
   * 最大访问次数.
   *
   * @return the int
   */
  int maxCount();
}
