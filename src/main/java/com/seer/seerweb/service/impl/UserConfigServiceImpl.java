package com.seer.seerweb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seer.seerweb.entity.UserConfig;
import com.seer.seerweb.service.UserConfigService;
import com.seer.seerweb.mapper.UserConfigMapper;
import org.springframework.stereotype.Service;

/**
* @author Glory
* @description 针对表【UserConfig】的数据库操作Service实现
* @createDate 2023-07-24 15:48:17
*/
@Service
public class UserConfigServiceImpl extends ServiceImpl<UserConfigMapper, UserConfig>
    implements UserConfigService{

}




