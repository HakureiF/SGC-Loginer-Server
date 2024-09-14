package com.seer.seerweb.controller;

import com.seer.seerweb.entity.dto.RacegroupDTO;
import com.seer.seerweb.service.RacegroupService;
import com.seer.seerweb.utils.ResultUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * The type Racegroup controller.
 */
@RestController
@RequestMapping("/api/race-group")
public class RacegroupController {
  /**
   * The Racegroup service.
   */
  @Autowired
  RacegroupService racegroupService;

  /**
   * Gets groups.
   *
   * @param userid the userid
   * @return the groups
   */
  /*
   * 获取当前用户加入的比赛组
   */
  @GetMapping("/groups")
  @ResponseBody
  public ResultUtil<List<RacegroupDTO>> getGroups(@RequestHeader("seer-userid")String userid) {
    List<RacegroupDTO> raceGroups = racegroupService.getGroups(userid);
    return ResultUtil.success(raceGroups);
  }

  /**
   * Search groups result util.
   *
   * @param group the group
   * @return the result util
   */
  /*
   * 查询比赛组
   */
  @GetMapping("/searchGroup")
  @ResponseBody
  public ResultUtil<List<RacegroupDTO>> searchGroups(@RequestParam("group") String group) {
    List<RacegroupDTO> racegroups = racegroupService.searchGroups(group);
    return ResultUtil.success(racegroups);
  }
}
