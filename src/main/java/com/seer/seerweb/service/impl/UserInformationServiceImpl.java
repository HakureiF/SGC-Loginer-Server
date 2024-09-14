package com.seer.seerweb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seer.seerweb.entity.UserInformation;
import com.seer.seerweb.mapper.UserInformationMapper;
import com.seer.seerweb.service.UserInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * The type User information service.
 */
@Service
public class UserInformationServiceImpl extends ServiceImpl<UserInformationMapper, UserInformation>
    implements UserInformationService{
  @Autowired
  private UserInformationMapper userInformationMapper;
  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  // 如果存在id则返回true
  @Override
  public boolean existId(String userid) {
    return !userInformationMapper.mapperExistId(userid).isEmpty();
  }

  @Override
  public boolean verifyLogin(String userid, String password) {
    return Objects.equals(userInformationMapper.selectPasswordById(userid), password);
  }

  @Override
  public boolean checkToken(String phone, String token, String type) {
    return Objects.equals(redisTemplate.opsForValue().get(phone + "token:" + type), token);
  }

  @Override
  public String getNickNameById(String id) {
    return userInformationMapper.selectNicknameByUserid(id);
  }

}




