package com.seer.seerweb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seer.seerweb.entity.Racegroup;
import com.seer.seerweb.entity.dto.RacegroupDTO;

import java.util.List;

public interface RacegroupService extends IService<Racegroup> {
    List<RacegroupDTO> getGroups(String userid);
    void initElfPool(String groupId, String elfPool);
    void initLimitPool(String groupId, String limitstr, String awardstr, String punishStr);
    List<RacegroupDTO> searchGroups(String group);
}
