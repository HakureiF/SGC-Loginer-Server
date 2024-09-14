package com.seer.seerweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seer.seerweb.entity.Racegroup;
import com.seer.seerweb.entity.dto.RacegroupDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface RacegroupMapper extends BaseMapper<Racegroup> {
    List<RacegroupDTO> onesRaceGroups(String userid);

    List<RacegroupDTO> searchGroups(String group);
}
