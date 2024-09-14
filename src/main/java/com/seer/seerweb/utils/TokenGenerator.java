package com.seer.seerweb.utils;

import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.mapper.UserInformationMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * The type Token generator.
 */
@Component
public class TokenGenerator {
  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  UserInformationMapper userInformationMapper;
  @Autowired
  ConfigContent configContent;

  public String generator(String userid, String origin) {
    String token = RandomStringUtils.randomAlphanumeric(12);
    redisTemplate.opsForHash().put(userid,"token:" + origin,token);
    redisTemplate.opsForHash().put(userid,"type",userInformationMapper.selectTypeByUserid(userid));
    redisTemplate.expire(userid,configContent.getTokenTime(), TimeUnit.HOURS);
    return token;
  }
  public String generator(String phone,String code,String type) {
    String token = RandomStringUtils.randomAlphanumeric(12) + code;
    redisTemplate.opsForValue().set(phone + "token:" + type,token);
    redisTemplate.expire(phone + "token:" + type,5, TimeUnit.MINUTES);
    return token;
  }
  public String generator(int num) {
    Random random = new Random();
    StringBuilder code = new StringBuilder(4);
    for (int i =0; i < num ; ++i) {
      int nextInt = random.nextInt(10);
      code.append(nextInt);
    }
    return code.toString();
  }
}
