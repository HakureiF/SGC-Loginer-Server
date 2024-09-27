package com.seer.seerweb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.entity.Racegroup;
import com.seer.seerweb.entity.SeerElf;
import com.seer.seerweb.entity.dto.LimitPoolDTO;
import com.seer.seerweb.entity.vo.*;
import com.seer.seerweb.service.ConventionalService;
import com.seer.seerweb.service.RacegroupService;
import com.seer.seerweb.service.SeerElfService;
import com.seer.seerweb.utils.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConventionalServiceImpl implements ConventionalService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedisTemplate<String, Integer> redisTemplateInt;
    @Autowired
    ConfigContent configContent;
    @Autowired
    RacegroupService racegroupService;
    @Autowired
    SeerElfService seerElfService;

    @Override
    public String verifyBag(VerifyBagVO bagVO) {
        long allStartTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        if (bagVO.getMatchGame() != null && bagVO.getMatchGame()) {
            String groupId = (String) redisTemplate.opsForHash().get("match-open", "groupId");
            if (groupId == null) return "获取不到比赛组";
            bagVO.setGroupId(groupId);
        }
        if (bagVO.getBagInfo() == null || bagVO.getBagInfo().isEmpty()) {
            return "获取不到背包内精灵数据";
        } else {
            bagVO.setBagInfo(seerElfService.checkAdvance(bagVO.getBagInfo()));
//            long executionTime = System.currentTimeMillis() - startTime;
//            log.info("检查神谕羁绊精灵耗时" + executionTime + "ms");
//            startTime = endTime;

            StringBuilder res = new StringBuilder();
            List<Integer> ids = bagVO.getBagInfo().stream().map(BagPetVO::getId).toList();
            List<Integer> errban = new ArrayList<>();
            List<Integer> errpool = new ArrayList<>();
//            HashMap<Integer, List<Integer>> errlimit = new HashMap<>();
//            for (int i=1; i<5; i++) {
//                errlimit.put(i, new ArrayList<>());
//            }
            List<LimitPoolDTO> errlimit = new ArrayList<>();

            Racegroup racegroup = new Racegroup();
            if (bagVO.getGameId() != null) {
                String gameId = "game" + bagVO.getGameId();
                String groupId = (String) Objects.requireNonNull(redisTemplate.opsForHash().get(gameId, "groupId"));
                racegroup = racegroupService.getById(groupId);
                boolean poolEnable = Objects.equals(redisTemplate.opsForHash().get(gameId, "EnableElfPool"), "enable");
                List<Integer> elfBan = new ArrayList<>();
                if (Objects.equals(redisTemplate.opsForHash().get(gameId, "EnableOfficialBan"), "enable")) {
                    //公ban开启
                    elfBan = JSON.parseArray((String) Objects.requireNonNull(redisTemplate.opsForHash().get(gameId, "OfficialBan"))).toJavaList(Integer.class);
                }

                Set<String> limitKeys = redisTemplate.keys("Group" + groupId + "limit*");
                int prefixLen = ("Group" + groupId + "limit").length();
                if (limitKeys != null) {
                    for (String limitKey : limitKeys) {
                        LimitPoolDTO limitPoolDTO = new LimitPoolDTO(Integer.parseInt(limitKey.substring(prefixLen, prefixLen+1)));
                        for (Integer id: ids){
                            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(limitKey, String.valueOf(id)))) {
                                limitPoolDTO.getCount().add(id);
                            }
                        }
                        errlimit.add(limitPoolDTO);
                    }
                }
                for (Integer id: ids){
                    if (poolEnable){
                        //精灵池开启
                        if (Boolean.FALSE.equals(redisTemplate.opsForHash().hasKey("Group" + groupId + "ElfPool", String.valueOf(id)))) {
                            //精灵池中没有精灵
                            errpool.add(id);
                        }
                    }
                    if (elfBan.contains(id)){
                        //公ban中有精灵
                        errban.add(id);
                    }
                }
//                executionTime = System.currentTimeMillis() - startTime;
//                log.info("加入对局检查耗时" + executionTime + "ms");
//                startTime = endTime;
            } else if (bagVO.getGroupId() != null) {
                //包含groupId但不包含gameId，是创建比赛
                String groupId = bagVO.getGroupId();
                racegroup = racegroupService.getById(groupId);
                String EnableOfficialBan = racegroup.getEnableBan();
                String EnableElfPool = racegroup.getEnablePool();
                if(EnableElfPool.equals("enable")){
                    //精灵池开启，如果redis中没有比赛组的精灵池，则先录入进redis
                    if(Boolean.TRUE.equals(redisTemplate.hasKey("Group" + racegroup.getGroupId() + "ElfPool"))){ // 检查redis中有无精灵池，没有则添加
                        redisTemplate.expire("Group" + racegroup.getGroupId() + "ElfPool", 24, TimeUnit.HOURS);
                    } else {
                        racegroupService.initElfPool(racegroup.getGroupId(), racegroup.getPickElfList());
                    }
                }
                racegroupService.initLimitPool(racegroup.getGroupId(), racegroup.getLimitPool(), racegroup.getAwardPool(), racegroup.getPunishPool());
                List<Integer> elfBan = new ArrayList<>();
                if(EnableOfficialBan.equals("enable")){
                    //公ban开启
                    elfBan = JSON.parseArray(racegroup.getBanElfList()).toJavaList(Integer.class);
                }
                Map<String, JSONArray> limit = JSON.parseObject(racegroup.getLimitPool(), Map.class);
                if (limit != null) {
                    for (Map.Entry<String, JSONArray> entry: limit.entrySet()){
                        String key = entry.getKey();
                        JSONArray value = entry.getValue();
                        LimitPoolDTO limitPoolDTO = new LimitPoolDTO(Integer.parseInt(key.substring(5,6)));
                        for (Integer id: ids){
                            if (value.contains(id)) {
                                limitPoolDTO.getCount().add(id);
                            }
                        }
                        errlimit.add(limitPoolDTO);
                    }
                }
                for (Integer id: ids){
                    if (EnableElfPool.equals("enable")){
                        //精灵池开启
                        if (Boolean.FALSE.equals(redisTemplate.opsForHash().hasKey("Group" + groupId + "ElfPool", String.valueOf(id)))) {
                            //精灵池中没有精灵
                            errpool.add(id);
                        }
                    }
                    if (elfBan.contains(id)){
                        //公ban中有精灵
                        errban.add(id);
                    }
                }
//                executionTime = System.currentTimeMillis() - startTime;
//                log.info("创建对局检查耗时" + executionTime + "ms");
//                startTime = endTime;
            } else {
                return "没有比赛组数据";
            }

            if (racegroup.getThirdMark().equals("forbid")) {
                for (BagPetVO bagPetVO: bagVO.getBagInfo()) {
                    int tmp = 0;
                    for (Integer markId: bagPetVO.getMarks()) {
                        if (markId != 0) tmp ++;
                    }
                    if (tmp >= 3) return "当前模式限制为2个刻印，如有三孔精灵请卸下一个刻印";
                }
            }
//            executionTime = System.currentTimeMillis() - startTime;
//            log.info("检测三孔耗时" + executionTime + "ms");
//            startTime = endTime;

            if (racegroup.getLimitMarks() != null) {
                List<Integer> limitMarks = Arrays.stream(racegroup.getLimitMarks().split(",")).map(Integer::parseInt).toList();
                for (BagPetVO bagPetVO: bagVO.getBagInfo()) {
                    for (MarkVO markVO: bagPetVO.getBindMarks()) {
                        if (markVO.get_obtainTime() !=0 && bagPetVO.getMarks().contains(markVO.get_obtainTime()) && !limitMarks.contains(markVO.get_markID())) {
                            // 当前刻印在装备的刻印中，同时不在限定的刻印列表里
                            return "当前模式限制指定刻印，请检查精灵背包";
                        }
                    }
                }
            }
//            executionTime = System.currentTimeMillis() - startTime;
//            log.info("检测限定刻印耗时" + executionTime + "ms");
//            startTime = endTime;

            if (racegroup.getMarkStone().equals("forbid")) {
                for (BagPetVO bagPetVO: bagVO.getBagInfo()) {
                    for (MarkVO markVO: bagPetVO.getBindMarks()) {
                        if (markVO.get_bindMoveID() != null && markVO.get_bindMoveID() != 0) {
                            // 发现一个绑定了技能的刻印，检查技能列表中是否携带该技能
                            for (Skill skill: bagPetVO.getSkillArray()) {
                                if (skill.get_id() == markVO.get_bindMoveID()) {
                                    return "当前模式禁止使用刻印宝石";
                                }
                            }
                        }
                    }
                }
            }
//            executionTime = System.currentTimeMillis() - startTime;
//            log.info("检测刻印宝石耗时" + executionTime + "ms");
//            startTime = endTime;

            if (!errban.isEmpty()){
                List<String> errname = seerElfService.listByIds(errban).stream().map(SeerElf::getDefname).collect(Collectors.toList());
                res.append("携带ban内精灵：").append(String.join("，", errname)).append("。\n");
            }
            if (!errpool.isEmpty()) {
                List<String> errname = seerElfService.listByIds(errpool).stream().map(SeerElf::getDefname).collect(Collectors.toList());
                res.append("携带限定池外精灵：").append(String.join("，", errname)).append("。\n");
            }
            for (LimitPoolDTO limitPoolDTO: errlimit) {
                if (limitPoolDTO.getCount().size() > limitPoolDTO.getLimitNum()) {
                    List<String> errname = seerElfService.listByIds(limitPoolDTO.getCount()).stream().map(SeerElf::getDefname).toList();
                    res.append("限").append(limitPoolDTO.getLimitNum()).append("精灵超量：").append(String.join("，", errname)).append("。\n");
                }
            }
//            executionTime = System.currentTimeMillis() - startTime;
//            log.info("ban和限定转换耗时" + executionTime + "ms");

            long executionTime = System.currentTimeMillis() - allStartTime;
            log.info("校验背包耗时" + executionTime + "ms");
            return res.toString();
        }
    }

    @Override
    public String freshBag(String id, List<BagPetVO> vo) {
        String userid = AesUtil.decrypt(id, configContent.getAesKey());
        String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
        if (gameId != null) {
            if (Objects.equals(redisTemplate.opsForHash().get(gameId, "phase"), "PreparationStage") ||
                    Objects.equals(redisTemplate.opsForHash().get(gameId, "phase"), "WaitingStage") ||
                    (Objects.equals(redisTemplate.opsForHash().get(gameId, "phase"), "ReadyStage") && Objects.equals(redisTemplate.opsForHash().get(gameId, "Player1"), userid))){
                //更新数据
                vo = seerElfService.checkAdvance(vo);
                VerifyBagVO verifyBagVO = new VerifyBagVO();
                verifyBagVO.setBagInfo(vo);
                verifyBagVO.setGameId(gameId.substring(4));
                String mess = verifyBag(verifyBagVO);
                if (!mess.isBlank()) {
                    return mess;
                }
                if (Objects.equals(redisTemplate.opsForHash().get(gameId, "Player1"), userid)){
                    redisTemplate.opsForHash().put(gameId, "Player1PetState", JSON.toJSONString(vo));
                    calcStoreScore(gameId, "Player1", vo);
                } else if (Objects.equals(redisTemplate.opsForHash().get(gameId, "Player2"), userid)) {
                    redisTemplate.opsForHash().put(gameId, "Player2PetState", JSON.toJSONString(vo));
                    calcStoreScore(gameId, "Player2", vo);
                }
            }
            return "";
        }
        return "未知错误";
    }

    private void calcStoreScore(String gameId, String side, List<BagPetVO> vo) {
        String raceScoreKey = (String) redisTemplate.opsForHash().get("match-open", "raceScoreKey");
        Integer initialScore = (Integer) redisTemplate.opsForHash().get("match-open", "initialScore");
        String groupId = (String) redisTemplate.opsForHash().get(gameId, "groupId");
        if (raceScoreKey != null && initialScore != null) {
            int score = initialScore;
            Set<String> punishKeys = redisTemplate.keys("Group" + groupId + "Punish" + "*");
            if (punishKeys != null) {
                for (BagPetVO pet: vo) {
                    for (String punishKey : punishKeys) {
                        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(punishKey, String.valueOf(pet.getId())))) {
                            int tmp = punishKey.lastIndexOf("Punish");
                            if (tmp > 0) {
                                int scorePunish = Integer.parseInt(punishKey.substring(tmp + 6));
                                score -= scorePunish;
                            }
                        }
                    }
                }
            }
            Set<String> awardKeys = redisTemplate.keys("Group" + groupId + "Award" + "*");
            if (awardKeys != null) {
                for (BagPetVO pet: vo) {
                    for (String awardKey : awardKeys) {
                        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(awardKey, String.valueOf(pet.getId())))) {
                            int tmp = awardKey.lastIndexOf("Award");
                            if (tmp > 0) {
                                int scoreAward = Integer.parseInt(awardKey.substring(tmp + 5));
                                score += scoreAward;
                            }
                        }
                    }
                }
            }
            String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
            String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
            if (side.equals("Player1")) {
                redisTemplate.opsForHash().put(raceScoreKey, player1 + "-" + player2, score);
            }
            if (side.equals("Player2")) {
                redisTemplate.opsForHash().put(raceScoreKey, player2 + "-" + player1, score);
            }
        }
    }

    @Override
    public Map<String, List<BagPetVO>> getPetState(String id) throws Exception {
        String userid = AesUtil.decrypt(id, configContent.getAesKey());
        String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
        if (gameId != null) {
            //是否能看见对手背包阶段
            Object phase = redisTemplate.opsForHash().get(gameId, "phase");
            boolean rivalBagVisible = !Objects.equals(phase, "WaitingStage")
                    && !Objects.equals(phase, "PreparationStage")
                    && !Objects.equals(phase, "ReadyStage");
            //是否能看见我方被ban精灵阶段
            boolean bannedVisible = rivalBagVisible && !Objects.equals(phase, "PlayerBanElf");

            List<BagPetVO> player1PetState = JSON.parseArray((String) redisTemplate.opsForHash().get(gameId, "Player1PetState"), BagPetVO.class);
            List<BagPetVO> player2PetState = JSON.parseArray((String) redisTemplate.opsForHash().get(gameId, "Player2PetState"), BagPetVO.class);
            Map<String, List<BagPetVO>> res = new HashMap<>();

            if (Objects.equals(redisTemplate.opsForHash().get(gameId, "Player1"), userid)) {
                // Player1请求
                if (player2PetState != null ){
                    for (BagPetVO pet: player2PetState) {
                        if (pet.getState() == 2 || pet.getState() == 3) {
                            pet.setState(0);
                        }
                    }
                    if (!rivalBagVisible) {
                        player2PetState= null;
                    }
                }
                if (player1PetState != null && !bannedVisible) {
                    for (BagPetVO pet: player1PetState) {
                        if (pet.getState() == 1) {
                            pet.setState(0);
                        }
                    }
                }
            }  else if (Objects.equals(redisTemplate.opsForHash().get(gameId, "Player2"), userid)) {
                // Player2请求
                if (player1PetState != null ){
                    for (BagPetVO pet: player1PetState) {
                        if (pet.getState() == 2 || pet.getState() == 3) {
                            pet.setState(0);
                        }
                    }
                    if (!rivalBagVisible) {
                        player1PetState= null;
                    }
                }
                if (player2PetState != null && !bannedVisible) {
                    for (BagPetVO pet: player2PetState) {
                        if (pet.getState() == 1) {
                            pet.setState(0);
                        }
                    }
                }
            }
            res.put("Player1PetState", player1PetState);
            res.put("Player2PetState", player2PetState);
            return res;
        }
        throw new Exception("对局不存在");
    }


    @Override
    public Integer scoreReduced(String groupId, List<BagPetVO> bagPetVOList, boolean isOffline) {
        int scoreReduced = 0;
//        Set<String> limitKeys = redisTemplate.keys("Group" + groupId + "Limit" + "*");
//        if (limitKeys != null) {
//            for (BagPetVO pet: bagPetVOList) {
//                for (String limitKey : limitKeys) {
//                    if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(limitKey, String.valueOf(pet.getId())))) {
//                        scoreReduced += (4-i);
//                    }
//                }
//            }
//        }
        for (BagPetVO pet: bagPetVOList) {
            for (int i=1; i<4; i++) {
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Group" + groupId + "Limit" + i)) &&
                        Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("Group" + groupId + "Limit" + i, String.valueOf(pet.getId())))) {
                    scoreReduced += (4-i);
                }
            }
        }
        if (isOffline) {
            return scoreReduced;
        }
        Set<String> awardKeys = redisTemplate.keys("Group" + groupId + "Award" + "*");
        if (awardKeys != null) {
            for (BagPetVO pet: bagPetVOList) {
                if (pet.getState() == 2 || pet.getState() == 3) {
                    for (String awardKey : awardKeys) {
                        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(awardKey, String.valueOf(pet.getId())))) {
                            int tmp = awardKey.lastIndexOf("Award");
                            if (tmp > 0) {
                                int scoreAward = Integer.parseInt(awardKey.substring(tmp + 5));
                                scoreReduced -= scoreAward;
                            }
                        }
                    }
                }
            }
        }
        return scoreReduced;
    }

    @Override
    public List<Map<String, Object>> getMatchScoreBoard(String groupId) {
        List<Map<String, Object>> res = new ArrayList<>();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("ScoreBoard" + groupId);
        for (Object o: entries.keySet()) {
            String mimiId = (String) o;
//            StringBuilder sb = new StringBuilder((String) o);
//            for (int i=0; i<sb.length(); i++) {
//                if (i>3 && i<sb.length()-1) {
//                    sb.setCharAt(i, '*');
//                }
//            }
            Map<String, Object> map = new HashMap<>();
            map.put("mimiId", mimiId);
            map.put("score", entries.get(o));
            res.add(map);
        }
        res = res.stream().sorted(Comparator.comparing(a ->
            (Integer) ((Map<String, Object>) a).get("score")
        ).reversed()).collect(Collectors.toList());
        return res;
    }

    @Override
    public String verifySuit(VerifySuitVO suitVO) {
        if (suitVO.getMatchGame() != null && suitVO.getMatchGame()) {
            String groupId = (String) redisTemplate.opsForHash().get("match-open", "groupId");
            if (groupId == null) return "获取不到比赛组";
            suitVO.setGroupId(groupId);
        }
        if (suitVO.getSuitId() == null || suitVO.getSuitId() == 0) {
            return "获取不到套装";
        } else {
            Racegroup racegroup;
            if (suitVO.getGameId() != null) {
                // 加入对局
                String gameId = suitVO.getGameId();
                String groupId = (String) Objects.requireNonNull(redisTemplate.opsForHash().get(gameId, "groupId"));
                racegroup = racegroupService.getById(groupId);
            } else if (suitVO.getGroupId() != null) {
                String groupId = suitVO.getGroupId();
                racegroup = racegroupService.getById(groupId);
            } else {
                return "获取不到比赛组";
            }

            if (racegroup.getDisabledSuits() != null && racegroup.getDisabledSuits().contains(suitVO.getSuitId().toString())) {
                return "穿戴了禁用的套装";
            }
            return "";
        }
    }

    @Override
    public Integer getBanNum(String id) {
        String userid = AesUtil.decrypt(id, configContent.getAesKey());
        String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
        if (gameId != null) {
            Integer banNum = (Integer) redisTemplate.opsForHash().get(gameId,"banNum");
            if (banNum != null) {
                return banNum;
            }
        }
        return 3;
    }
}
