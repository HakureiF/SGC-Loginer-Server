package com.seer.seerweb.service.impl;

import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seer.seerweb.entity.AttackLog;
import com.seer.seerweb.service.AttackLogService;
import com.seer.seerweb.mapper.AttackLogMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author Glory
* @description 针对表【AttackLog】的数据库操作Service实现
* @createDate 2023-07-23 09:53:28
*/
@Service
public class AttackLogServiceImpl extends ServiceImpl<AttackLogMapper, AttackLog>
    implements AttackLogService{

  /**
   * @param data
   * @return
   */
  @Override
  public boolean sqlCheck(String data, String ip) {
    if (SqlInjectionUtils.check(data)) {
      AttackLog attackLog = new AttackLog();
      attackLog.setType("SQL注入攻击");
      attackLog.setIp(ip);
      attackLog.setDate(new Date());
      save(attackLog);
      return true;
    }
    return false;
  }
}




