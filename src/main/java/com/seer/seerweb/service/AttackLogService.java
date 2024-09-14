package com.seer.seerweb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seer.seerweb.entity.AttackLog;

/**
 * The interface Attack log service.
 *
 * @author Glory
 * @description 针对表 【AttackLog】的数据库操作Service
 * @createDate 2023 -07-23 09:53:28
 */
public interface AttackLogService extends IService<AttackLog> {
  /**
   * Sql check boolean.
   *
   * @param data the data
   * @param ip   the ip
   * @return the boolean
   */
  boolean sqlCheck(String data, String ip);
}
