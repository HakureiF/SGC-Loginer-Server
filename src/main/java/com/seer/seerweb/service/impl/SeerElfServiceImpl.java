package com.seer.seerweb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seer.seerweb.entity.SeerElf;
import com.seer.seerweb.entity.vo.BagPetVO;
import com.seer.seerweb.mapper.SeerElfMapper;
import com.seer.seerweb.service.SeerElfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
* @author Glory
* {@code @description} 针对表【SeerElf】的数据库操作Service实现
* {@code @createDate} 2023-06-22 23:15:10
 */
@Service
@Slf4j
public class SeerElfServiceImpl extends ServiceImpl<SeerElfMapper, SeerElf>
    implements SeerElfService{
  @Autowired
  SeerElfMapper seerElfMapper;

  @Autowired
  RedisTemplate<String, Integer> redisTemplateInt;

  @Override
  public List<BagPetVO> checkAdvance(List<BagPetVO> vo) {
    for(BagPetVO pet: vo) {
      if (pet.getEffectID() != 0) {
        Integer id = seerElfMapper.checkEffectId(pet.getEffectID());
        if (id != null) pet.setId(id);
      }
    }
    return vo;
  }

  public Integer checkEffectId(Integer effectId) {
    Integer petId = (Integer) redisTemplateInt.opsForHash().get("effectIdMap", String.valueOf(effectId));
    if (petId == null) {
      petId = seerElfMapper.checkEffectId(effectId);
      redisTemplateInt.opsForHash().put("effectIdMap", String.valueOf(effectId), petId);
    }
    return petId;
  }
}




