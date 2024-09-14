package com.seer.seerweb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seer.seerweb.entity.Announcement;
import com.seer.seerweb.service.AnnouncementService;
import com.seer.seerweb.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
* @author Glory
* @description 针对表【announcement】的数据库操作Service实现
* @createDate 2023-09-19 17:52:14
*/
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
    implements AnnouncementService{

  @Autowired
  AnnouncementMapper announcementMapper;

  @Autowired
  RedisTemplate<String, String> redisTemplate;

  @Override
  public HashMap<String, String> getLoginerAnnouncement() {
    HashMap<String, String> res = new HashMap<>();

    Date now = new Date();
    List<String> loginerAnnouncements = announcementMapper.getLoginerAnnouncement(now);
    loginerAnnouncements.forEach(announcement -> {
      if (announcement.startsWith("version")) {
        res.put("version", announcement.substring(7, 12));
        res.put("download", announcement.substring(12));
      } else {
        res.put("announcement", announcement);
      }
    });
    return res;
  }
}




