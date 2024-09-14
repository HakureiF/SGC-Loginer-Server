package com.seer.seerweb.controller;

import com.seer.seerweb.annotation.AccessLimit;
import com.seer.seerweb.service.AnnouncementService;
import com.seer.seerweb.utils.ResultUtil;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器.
 * </p>
 *
 * @author rido
 * @since 2023-09-19
 */
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {
  @Autowired
  AnnouncementService announcementService;

  @GetMapping("/getLoginerAnnouncement")
  @ResponseBody
  @AccessLimit(seconds = 2, maxCount = 20)
  public ResultUtil<HashMap<String, String>> getLoginerAnnouncement() {
    return ResultUtil.success(announcementService.getLoginerAnnouncement());
  }
}
