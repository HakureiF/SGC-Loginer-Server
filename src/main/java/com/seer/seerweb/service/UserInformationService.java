package com.seer.seerweb.service;

import com.seer.seerweb.entity.UserInformation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.seer.seerweb.utils.ResultUtil;

import java.io.IOException;
import java.util.HashMap;

/**
* @author Glory
* {@code @description} 针对表【UserInformation】的数据库操作Service
* {@code @createDate} 2023-06-23 20:59:48
 */
public interface UserInformationService extends IService<UserInformation> {
  boolean existId(String userid);
  boolean verifyLogin(String userid,String password);
  boolean checkToken(String phone,String token, String type);
  String getNickNameById(String id);
}
