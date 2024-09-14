package com.seer.seerweb.service;

import com.seer.seerweb.entity.Announcement;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;
import java.util.List;

/**
* @author Glory
* @description 针对表【announcement】的数据库操作Service
* @createDate 2023-09-19 17:52:14
*/
public interface AnnouncementService extends IService<Announcement> {
  HashMap<String, String> getLoginerAnnouncement();
}
