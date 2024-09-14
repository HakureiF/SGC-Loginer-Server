package com.seer.seerweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seer.seerweb.entity.SeerElf;
import com.seer.seerweb.entity.vo.BagPetVO;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author Glory
* {@code @description} 针对表【SeerElf】的数据库操作Mapper
* {@code @createDate} 2023-06-22 23:15:10
* {@code @Entity} com.seer.seerweb.entity.SeerElf
 */
@Mapper
public interface SeerElfMapper extends BaseMapper<SeerElf> {
  @Select("select ID from SeerElf where effectID = #{effectID}")
  Integer checkEffectId(@Param("effectID") Integer effectID);
}




