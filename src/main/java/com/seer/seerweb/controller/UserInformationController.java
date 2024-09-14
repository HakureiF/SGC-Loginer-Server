package com.seer.seerweb.controller;

import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.service.UserInformationService;
import com.seer.seerweb.utils.AesUtil;
import com.seer.seerweb.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author chen
 * @since 2023 -04-19
 */
@RestController
@RequestMapping("/api/user-information")
public class UserInformationController {
  /**
   * The User information service.
   */
  @Autowired
  UserInformationService userInformationService;
  @Autowired
  ConfigContent configContent;

  /**
   * Gets nickname.
   *
   * @param userid the userid
   * @return the nickname
   */
  @GetMapping("/getNickname")
  @ResponseBody
  public ResultUtil<String> getNickname(@RequestHeader("seer-userid") String userid) {
    String id = AesUtil.decrypt(userid,configContent.getAesKey());
    return ResultUtil.success(userInformationService.getNickNameById(id));
  }
}
