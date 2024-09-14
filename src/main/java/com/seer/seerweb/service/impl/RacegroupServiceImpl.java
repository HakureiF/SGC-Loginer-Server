package com.seer.seerweb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.entity.Racegroup;
import com.seer.seerweb.entity.dto.RacegroupDTO;
import com.seer.seerweb.mapper.RacegroupMapper;
import com.seer.seerweb.service.RacegroupService;
import com.seer.seerweb.utils.AesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RacegroupServiceImpl extends ServiceImpl<RacegroupMapper, Racegroup> implements RacegroupService {
    @Autowired
    RacegroupMapper racegroupMapper;
    @Autowired
    ConfigContent configContent;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Override
    public List<RacegroupDTO> getGroups(String id) {
        String userid = AesUtil.decrypt(id, configContent.getAesKey());
        return racegroupMapper.onesRaceGroups(userid);
    }

    @Override
    public void initElfPool(String groupId, String elfPool) {
        List<Integer> elves = JSON.parseArray(elfPool).toJavaList(Integer.class);
        Map<String, Integer> pool = elves.stream().collect(Collectors.toMap(String::valueOf, id -> id));
        TreeMap<String, Integer> poolSorted = new TreeMap<>(
                (a, b) -> Integer.parseInt(b) - Integer.parseInt(a)
        );
        poolSorted.putAll(pool);
//        elves.forEach(elf -> redisTemplate.opsForHash().put("Group" + groupId + "ElfPool", String.valueOf(elf), elf));
        redisTemplate.opsForHash().putAll("Group" + groupId + "ElfPool", poolSorted);
        redisTemplate.expire("Group" + groupId + "ElfPool", 24, TimeUnit.HOURS);
    }

    @Override
    public void initLimitPool(String groupId, String limitstr, String awardstr) {
        if (limitstr != null && !limitstr.isEmpty()) {
            Map<String, JSONArray> limit = JSON.parseObject(limitstr, Map.class);
            for (Map.Entry<String, JSONArray> entry : limit.entrySet()) {
                String key = entry.getKey();
                JSONArray value = entry.getValue();
                for (Object id: value) {
                    redisTemplate.opsForSet().add("Group" + groupId + key, String.valueOf(id));
                }
                redisTemplate.expire("Group" + groupId + key, 12, TimeUnit.HOURS);
            }
//            for(int curr=1; curr<5; curr++){
//                if (limit.containsKey("limit" + curr) && Boolean.FALSE.equals(redisTemplate.hasKey("Group" + groupId + "Limit" + curr))) {
//                    for (Object id : limit.get("limit" + curr)) {
//                        redisTemplate.opsForSet().add("Group" + groupId + "Limit" + curr, String.valueOf(id));
//                    }
//                    redisTemplate.expire("Group" + groupId + "Limit" + curr, 12, TimeUnit.HOURS);
//                }
//            }
        }
        if (awardstr != null && !awardstr.isEmpty()) {
            Map<String, JSONArray> award = JSON.parseObject(awardstr, Map.class);
            for(int curr=1; curr<50; curr++){
                if (award.containsKey("award" + curr) && Boolean.FALSE.equals(redisTemplate.hasKey("Group" + groupId + "Award" + curr))) {
                    for (Object id : award.get("award" + curr)) {
                        redisTemplate.opsForSet().add("Group" + groupId + "Award" + curr, String.valueOf(id));
                    }
                    redisTemplate.expire("Group" + groupId + "Award" + curr, 12, TimeUnit.HOURS);
                }
            }
        }
    }

    @Override
    public List<RacegroupDTO> searchGroups(String group) {
        if(group == null || group.isBlank()) return new ArrayList<>();
        return racegroupMapper.searchGroups(group);
    }
}
