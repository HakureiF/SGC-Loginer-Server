package com.seer.seerweb.controller;

import com.alibaba.fastjson.JSON;
import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.entity.UserConfig;
import com.seer.seerweb.entity.UserInformation;
import com.seer.seerweb.service.UserConfigService;
import com.seer.seerweb.service.UserInformationService;
import com.seer.seerweb.utils.AesUtil;
import com.seer.seerweb.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author rido
 * @since 2023-07-24
 */
@RestController
@RequestMapping("/api/user-config")
public class UserConfigController {
  @Autowired
  ConfigContent configContent;
  @Autowired
  UserConfigService userConfigService;
  @Autowired
  UserInformationService userInformationService;


  @PostMapping("/sendElite")
  public ResultUtil<String> sendElite(@RequestHeader("seer-userid") String id, @RequestBody List<Integer> elites) {
    String userid = AesUtil.decrypt(id,configContent.getAesKey());
    UserConfig userConfig = userConfigService.getById(userid);
    if (userConfig == null) {
      UserInformation userInformation = userInformationService.getById(userid);
      if (userInformation == null) {
        return ResultUtil.fail();
      }
      userConfig = new UserConfig();
      userConfig.setUserid(userInformation.getUserid());
      userConfig.setHeadUrl("https://cdn.imrightchen.live/website/file/assets/origin/head.png");
    }
    userConfig.setElfLike(JSON.toJSONString(elites));
    userConfigService.saveOrUpdate(userConfig);
    return ResultUtil.success();
  }
}
