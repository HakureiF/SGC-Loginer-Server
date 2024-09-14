package com.seer.seerweb.controller;

import com.seer.seerweb.component.LoginerWS;
import com.seer.seerweb.service.UserInformationService;
import com.seer.seerweb.utils.ResultUtil;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * The type Login information controller.
 */
@RestController
@RequestMapping("/api/login-information")
public class LoginInformationController {

  @Autowired
  UserInformationService userInformationService;


  @PostMapping("/loginerlogin")
  @ResponseBody
  public ResultUtil<String> loginerLogin(@RequestBody HashMap<String, String> body){
    if (body != null && body.get("userid").length() <= 8) {
      if (!LoginerWS.checkLoginerState(body.get("userid"))) {
        return ResultUtil.fail("已处于在线状态", "");
      }
      if (userInformationService.verifyLogin(body.get("userid"), body.get("password"))) {
        return ResultUtil.success();
      }
    }
    return ResultUtil.fail("验证不通过", "");
  }
}
