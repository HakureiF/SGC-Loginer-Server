package com.seer.seerweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seer.seerweb.entity.Announcement;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
* @author Glory
* @description 针对表【announcement】的数据库操作Mapper
* @createDate 2023-09-19 17:52:14
* @Entity com.seer.seerweb.entity.Announcement
*/
public interface AnnouncementMapper extends BaseMapper<Announcement> {

  @Select("select content from Announcement " +
          "where deadline_date > cast(#{now} as datetime) and (type = 'both' or type = 'loginer')")
  List<String> getLoginerAnnouncement(@Param("now") Date now);
}




