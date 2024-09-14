package com.seer.seerweb.service;

import com.seer.seerweb.entity.SeerElf;
import com.baomidou.mybatisplus.extension.service.IService;
import com.seer.seerweb.entity.vo.BagPetVO;

import java.util.List;

/**
* @author Glory
* {@code @description} 针对表【SeerElf】的数据库操作Service
* {@code @createDate} 2023-06-22 23:15:10
 */
public interface SeerElfService extends IService<SeerElf> {
  List<BagPetVO> checkAdvance(List<BagPetVO> vo);
}
