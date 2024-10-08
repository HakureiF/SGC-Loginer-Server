package com.seer.seerweb.component;

import com.alibaba.fastjson.JSON;
import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.entity.vo.BagPetVO;
import com.seer.seerweb.service.ConventionalService;
import com.seer.seerweb.service.GameInformationService;
import com.seer.seerweb.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static com.seer.seerweb.component.LoginerWS.heartbeatAll;
import static com.seer.seerweb.component.LoginerWS.sendMessageById;

@Component
@Slf4j
public class ConventionalMode implements CommandLineRunner {
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    DelayQueueManager delayQueueManager;

    @Autowired
    ConfigContent configContent;

    @Autowired
    GameInformationService gameInformationService;

    @Autowired
    ConventionalService conventionalService;


    public void sendRoomId(String mes, String userid) {
        String player = getPlayerType(userid);
        if(player != null){
            Integer roomId = Integer.valueOf(mes.substring(6));
            String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
            if(gameId != null){
                redisTemplate.opsForHash().put(gameId, "RoomId", roomId);
                redisTemplate.opsForHash().put(gameId, "RoomOwner", player);
                //对手是否进入bp房，已进入则发送游戏房编号给对手登陆器
                String rival;
                if(player.equals("Player1")){
                    rival = (String) redisTemplate.opsForHash().get(gameId, "Player2");
                } else {
                    rival = (String) redisTemplate.opsForHash().get(gameId, "Player1");
                }
                if(rival != null && !rival.isBlank()){
                    sendMessageById(rival, "RoomId" + roomId);
                }
            }
        }
    }

    public void setMimiId(String mes, String userid){
        String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
        if (gameId != null){
            String type = (String) redisTemplate.opsForHash().get("game" +  userid,"type");
            redisTemplate.opsForHash().put(gameId, type + "mimiId", mes.substring(6));
        }
    }

    public void readyForStart(String userid){
        redisTemplate.delete("PickElfList" + userid);
        redisTemplate.delete("BanElfList" + userid);

        String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
        if (gameId != null){
            String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
            String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
            //检查是否待准备阶段
            if (Objects.equals(redisTemplate.opsForHash().get(gameId, "phase"), "PreparationStage")) {
                String type = (String) redisTemplate.opsForHash().get("game" +  userid,"type");
                //检查是否挑战方
                if (Objects.equals(type, "Player2")){
                    redisTemplate.opsForHash().put(gameId,"phase","ReadyStage");
                    sendMessageById(player1,"ReadyStage");
                    sendMessageById(player2,"ReadyStage");
                }
            } else {
                sendMessageById(userid,"Info成员未齐");
            }
        }
    }

    public void gameStart(String userid){
        redisTemplate.delete("PickElfList" + userid);
        redisTemplate.delete("BanElfList" + userid);

        String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
        if (gameId != null) {
            String Player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
            String Player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
            // 检查是否待开始阶段
            if (Objects.equals(redisTemplate.opsForHash().get(gameId, "phase"), "ReadyStage")) {
                String type = (String) redisTemplate.opsForHash().get("game" + userid, "type");
                // 检查是否房主
                if (Objects.equals(redisTemplate.opsForHash().get(gameId, "Judge"), userid)) {
                    redisTemplate.opsForHash().put(gameId, "phase", "PlayerBanElf");
                    // 开启计时器
                    delayQueueManager.taskConventionalBan(gameId, Player1, Player2);
                    sendMessageById(Player1, "PlayerBanElf");
                    sendMessageById(Player2, "PlayerBanElf");
                }
            } else {
                sendMessageById(userid, "Info成员未齐");
            }
        }
    }


    public void banElf(String mes, String userid){

        String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
        if (gameId != null) {
            String phase = (String) redisTemplate.opsForHash().get(gameId, "phase");
            if (phase == null || !phase.contains("BanElf")) {
                return;
            }
            List<Integer> elvesBan = JSON.parseArray(mes.substring(9)).toJavaList(Integer.class);
            Integer banNum = (Integer) redisTemplate.opsForHash().get(gameId, "banNum");
            if (banNum == null) banNum = 3;
            if (elvesBan.size() > banNum){
                elvesBan = elvesBan.subList(0, banNum);
            }
            String type = (String) redisTemplate.opsForHash().get("game" + userid, "type");
            if (type != null && Objects.equals(redisTemplate.opsForHash().get(gameId, type), userid)) {
                //检验是否是本人的消息，是则添加ban位
//                redisTemplate.opsForList().rightPushAll("BanElfList" + userid, elvesBan.stream().map(String::valueOf).collect(Collectors.toList()));
//                redisTemplate.expire("BanElfList" + userid, configContent.getGameTime(), TimeUnit.HOURS);
//
//                String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
//                String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
//                //同时有两方的ban位，表明ban结束
//                if (Boolean.TRUE.equals(redisTemplate.hasKey("BanElfList" + player1)) && Boolean.TRUE.equals(redisTemplate.hasKey("BanElfList" + player2))) {
//                    delayQueueManager.remove("PlayerBanElf", gameId);
//                    redisTemplate.opsForHash().put(gameId, "phase", "PlayerPickElfFirst");
//                    sendMessageById(player1, "PlayerPickElfFirst");
//                    sendMessageById(player2, "PlayerPickElfFirst");
//
//                    delayQueueManager.taskConventionalPickFirst(gameId, player1, player2);
//                }
                String rivalSide = type.equals("Player1") ? "Player2" : "Player1";
                String rivalpetStateStr = (String) redisTemplate.opsForHash().get(gameId, rivalSide + "PetState");
                if (rivalpetStateStr == null || rivalpetStateStr.contains("\"state\":1")) {
                    return;
                }
                List<BagPetVO> rivalpetState = JSON.parseArray(rivalpetStateStr, BagPetVO.class);
                if (rivalpetState == null) return;
                for (BagPetVO pet: rivalpetState) {
                    if (elvesBan.contains(pet.getId())) {
                        pet.setState(1);
                    }
                }
                redisTemplate.opsForHash().put(gameId, rivalSide + "PetState", JSON.toJSONString(rivalpetState));
                sendMessageById(userid, "BanOver");

                String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
                String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
//                List<BagPetVO> mypetState = JSON.parseArray((String) redisTemplate.opsForHash().get(gameId, type + "PetState"), BagPetVO.class);
                String mypetStateStr = (String) redisTemplate.opsForHash().get(gameId, type + "PetState");
                if (mypetStateStr != null && mypetStateStr.contains("\"state\":1")) {
                    delayQueueManager.remove("PlayerBanElf", gameId);
                    redisTemplate.opsForHash().put(gameId, "phase", "PlayerPickElfFirst");
                    sendMessageById(player1, "PlayerPickElfFirst");
                    sendMessageById(player2, "PlayerPickElfFirst");
                    delayQueueManager.taskConventionalPickFirst(gameId, player1, player2);
                }
            }
        }
    }

    public void pickElfFirst(String mes, String userid) {
        String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
        if (gameId != null) {
            Integer firstPick = Integer.valueOf(mes.substring(12));
            String phase = (String) redisTemplate.opsForHash().get(gameId, "phase");
            if (phase == null || !phase.contains("PickElfFirst")) {
                log.info("phase错误，不在Pick首发阶段");
                return;
            }
//            redisTemplate.opsForList().rightPush("PickElfList" + userid, firstPick);
//            redisTemplate.expire("PickElfList" + userid, configContent.getGameTime(), TimeUnit.HOURS);
//            String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
//            String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
//
//            //同时有两方的pick，表明两方都选好了首发
//            if (Boolean.TRUE.equals(redisTemplate.hasKey("PickElfList" + player1))) {
//                log.info("player1已经选好首发");
//                if (Boolean.TRUE.equals(redisTemplate.hasKey("PickElfList" + player2))) {
//                    log.info("player2已经选好首发");
//                    delayQueueManager.remove("PlayerPickElfFirst", gameId);
//                    redisTemplate.opsForHash().put(gameId, "phase", "PlayerPickElfRemain");
//                    sendMessageById(player1, "PlayerPickElfRemain");
//                    sendMessageById(player2, "PlayerPickElfRemain");
//
//                    delayQueueManager.taskConventionalPickRemain(gameId, player1, player2);
//                }
//            }

            String type = (String) redisTemplate.opsForHash().get("game" + userid, "type");
            if (type != null) {
                String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
                String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");

                String mypetStateStr = (String) redisTemplate.opsForHash().get(gameId, type + "PetState");
                if (mypetStateStr == null || mypetStateStr.contains("\"state\":2")) {
                    return;
                }
                List<BagPetVO> mypetState = JSON.parseArray(mypetStateStr, BagPetVO.class);
                if (mypetState == null) return;
                for (BagPetVO pet: mypetState) {
                    if (Objects.equals(pet.getId(), firstPick)) {
                        pet.setState(2);
                        break;
                    }
                }
                redisTemplate.opsForHash().put(gameId, type + "PetState", JSON.toJSONString(mypetState));
                sendMessageById(userid, "FirstOver");

                String rivalSide = type.equals("Player1") ? "Player2" : "Player1";
//                List<BagPetVO> rivalpetState = JSON.parseArray((String) redisTemplate.opsForHash().get(gameId, rivalSide + "PetState"), BagPetVO.class);
                String rivalpetStateStr = (String) redisTemplate.opsForHash().get(gameId, rivalSide + "PetState");
                if (rivalpetStateStr != null && rivalpetStateStr.contains("\"state\":2")) {
                    delayQueueManager.remove("PlayerPickElfFirst", gameId);
                    redisTemplate.opsForHash().put(gameId, "phase", "PlayerPickElfRemain");
                    sendMessageById(player1, "PlayerPickElfRemain");
                    sendMessageById(player2, "PlayerPickElfRemain");

                    delayQueueManager.taskConventionalPickRemain(gameId, player1, player2);
                }
            }
        }
    }

    public void pickElfRemain(String mes, String userid) {
        List<Integer> elvesRemain = JSON.parseArray(mes.substring(13))
                .toJavaList(Integer.class);
        if (elvesRemain.size() > 5){
            elvesRemain = elvesRemain.subList(0,5);
        }


        String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
        if (gameId != null) {
//            String phase = (String) redisTemplate.opsForHash().get(gameId, "phase");
//            if (phase == null || !phase.contains("PickElfRemain")) {
//                log.info("phase错误，不在pick出战阶段");
//                return;
//            }
//            redisTemplate.opsForList().rightPushAll("PickElfList" + userid, elvesRemain);
//            redisTemplate.expire("PickElfList" + userid, configContent.getGameTime(), TimeUnit.HOURS);
//            String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
//            String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
//
//            if (Objects.equals(redisTemplate.opsForList().size("PickElfList" + player1), 6L)) {
//                log.info("player1已经选好出战");
//                if (Objects.equals(redisTemplate.opsForList().size("PickElfList" + player2), 6L)) {
//                    log.info("player2已经选好出战");
//                    delayQueueManager.remove("PlayerPickElfRemain", gameId);
//                    redisTemplate.opsForHash().put(gameId, "phase", "WaitingPeriodResult");
//                    sendMessageById(player1, "WaitingPeriodResult");
//                    sendMessageById(player2, "WaitingPeriodResult");
//                }
//            }
            String type = (String) redisTemplate.opsForHash().get("game" + userid, "type");
            if (type != null) {
                String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
                String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
                List<BagPetVO> mypetState = JSON.parseArray((String) redisTemplate.opsForHash().get(gameId, type + "PetState"), BagPetVO.class);
                if (mypetState == null) return;
                for (BagPetVO pet: mypetState) {
                    if (elvesRemain.contains(pet.getId())) {
                        pet.setState(3);
                    }
                }
                redisTemplate.opsForHash().put(gameId, type + "PetState", JSON.toJSONString(mypetState));
                sendMessageById(userid, "RemainOver");

                String rivalSide = type.equals("Player1") ? "Player2" : "Player1";
//                List<BagPetVO> rivalpetState = JSON.parseArray((String) redisTemplate.opsForHash().get(gameId, rivalSide + "PetState"), BagPetVO.class);
                String rivalpetStateStr = (String) redisTemplate.opsForHash().get(gameId, rivalSide + "PetState");
                if (rivalpetStateStr != null && rivalpetStateStr.contains("\"state\":3")) {
                    delayQueueManager.remove("PlayerPickElfRemain", gameId);
                    redisTemplate.opsForHash().put(gameId, "phase", "WaitingPeriodResult");
                    sendMessageById(player1, "WaitingPeriodResult");
                    sendMessageById(player2, "WaitingPeriodResult");
                }
            }
        }
    }

    public void checkWinner(String mes, String userid) {
        String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
        log.info(userid + ": " + mes);
        if (gameId == null) return;
        redisTemplate.opsForHash().put(gameId, "phase", "PreparationStage");


        if (Objects.equals(mes.substring(8), "True")){
            //判断胜利双方
            if (userid.startsWith("seeraccount")) {
                String groupId = (String) redisTemplate.opsForHash().get(gameId, "groupId");
                if (groupId == null) return;
                //统计精灵数据
                String player1StateStr = (String) redisTemplate.opsForHash().get(gameId, "Player1PetState");
                String player2StateStr = (String) redisTemplate.opsForHash().get(gameId, "Player2PetState");
                if (player1StateStr != null) {
                    List<BagPetVO> player1State = JSON.parseArray(player1StateStr, BagPetVO.class);
                    for (BagPetVO pet: player1State) {
                        redisTemplate.opsForHash().increment("PickStatistics" + groupId, String.valueOf(pet.getId()), 1);
                    }
                }
                if (player2StateStr != null) {
                    List<BagPetVO> player2State = JSON.parseArray(player2StateStr, BagPetVO.class);
                    for (BagPetVO pet: player2State) {
                        redisTemplate.opsForHash().increment("PickStatistics" + groupId, String.valueOf(pet.getId()), 1);
                    }
                }

                // 统计分数
                String type = (String) redisTemplate.opsForHash().get("game" + userid, "type");
                if (type == null) return;
                String mimiId = userid.substring(11);
                int score = 15;
                List<BagPetVO> mypetState = JSON.parseArray(type.equals("Player1")? player1StateStr: player2StateStr, BagPetVO.class);
                score -= conventionalService.scoreReduced(groupId, mypetState, false);
                if (Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey("ScoreBoard" + groupId, mimiId))) {
                    redisTemplate.opsForHash().increment("ScoreBoard" + groupId, mimiId, score);
                } else {
                    redisTemplate.opsForHash().put("ScoreBoard" + groupId, mimiId, 1000 + score);
                }

                String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
                String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
                if (player1 != null && player2 != null) {
                    String side = player1.equals(userid) ? "player1": "player2";
                    redisTemplate.opsForHash().put(gameId, "fightResult", side + "Win");
                }


//                String raceCombKey = (String) redisTemplate.opsForHash().get("match-open", "raceCombKey");
//                if (raceCombKey != null) {
//
//                    if (player1 != null && player2 != null) {
//                        String side = player1.equals(userid) ? "player1": "player2";
//                        redisTemplate.opsForHash().put(raceCombKey, player1 + "-" + player2, side + "Win");
//                    }
//                }
            }
        }
    }

    public void afterFightOverClick(String userid) {
        if (userid.startsWith("seeraccount")) {
            String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
            if (gameId != null) {
                if (Objects.equals(userid, (String)redisTemplate.opsForHash().get(gameId, "Player1"))) {
                    sendMessageById(userid, "shutRoom");
                    if (Objects.equals(redisTemplate.opsForHash().get(gameId, "conventionalMode"), "common")) {
                        redisTemplate.delete(gameId);
                    }
                } else {
                    sendMessageById(userid, "quitRoom");
                }
            }
            redisTemplate.delete("game" + userid);



//            List<String> pick = redisTemplate.opsForList().range("PickElfList" + userid,0,-1);
//            List<String> ban = redisTemplate.opsForList().range("BanElfList" + userid,0,-1);
//            try {
//                for (String id:pick) {
//                    if (redisTemplate.opsForHash().get("PickStatistics", id) != null){
//                        redisTemplate.opsForHash().put("PickStatistics", id, (Integer) redisTemplate.opsForHash().get("PickStatistics", id) +1);
//                    } else {
//                        redisTemplate.opsForHash().put("PickStatistics", id, 1);
//                    }
//                }
//                for (String id:ban) {
//                    if (redisTemplate.opsForHash().get("BanStatistics", id) != null){
//                        redisTemplate.opsForHash().put("BanStatistics", id, (Integer) redisTemplate.opsForHash().get("BanStatistics", id) +1);
//                    } else {
//                        redisTemplate.opsForHash().put("BanStatistics", id, 1);
//                    }
//                }
//            } catch (Exception e){
//                log.error("存取精灵bp数据异常");
//                e.printStackTrace();
//            }
        }
        redisTemplate.delete("PickElfList" + userid);
        redisTemplate.delete("BanElfList" + userid);
    }

    public void endGame(String userid){
        String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
        String Player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
        String Player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
        Object Judge = redisTemplate.opsForHash().get(gameId, "Judge");
        if (Objects.equals(Judge, userid)) {
            for (DelayTask delayTask : delayQueueManager.getDelayQueue()) {
                if (delayTask.gameId().equals(gameId)) {
                    delayQueueManager.remove(delayTask);
                }
            }
            if (Judge != null) {
                // TODO 保存对局结果
            }
            redisTemplate.delete("BanElfList" + Player1);
            redisTemplate.delete("BanElfList" + Player2);
            redisTemplate.delete("game" + Player1);
            redisTemplate.delete("game" + Player2);
            redisTemplate.delete(gameId);
        }
    }

    /**
     * 匹配中掉线超过10秒，关闭对局
     * @param userid
     */
    public void offLineWhenMatch(String userid) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey("game" + userid))) {
            String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
            if (gameId == null) return;
//            delayQueueManager.taskMatchOffline(gameId, userid);
            delayQueueManager.handleMatchOffline(gameId, "MatchOffLine" + userid);
        }
    }

    /**
     * 匹配中重连
     * @param userid
     */
    public void reconnectMatch(String userid) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey("game" + userid))) {
            String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
            if (gameId != null && Boolean.TRUE.equals(redisTemplate.hasKey(gameId))) {
                delayQueueManager.remove("MatchOffLine" + userid, gameId);
            } else {
                redisTemplate.delete("PickElfList" + userid);
                redisTemplate.delete("BanElfList" + userid);
                redisTemplate.delete("game" + userid);
            }
        }
    }

    /**
     * 获取该userid用户对应的对局中type
     * @param userid
     * @return
     */
    private String getPlayerType(@NotNull String userid) {
        //判断有无对局信息
        if(Boolean.TRUE.equals(redisTemplate.hasKey("game" + userid))){
            String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
            if(gameId == null) {
                return null;
            }
            //判断是否为选手
            if(Objects.equals(redisTemplate.opsForHash().get(gameId, "Player1"), userid)){
                return "Player1";
            } else if(Objects.equals(redisTemplate.opsForHash().get(gameId, "Player2"), userid)){
                return "Player2";
            }
        }
        return null;
    }



    /**
     * 匹配队列
     */
    private final LinkedBlockingQueue<String> matchQueue = new LinkedBlockingQueue<>();

    public void joinMatch(String userid) {
        String raceCountKey = (String) redisTemplate.opsForHash().get("match-open", "raceCountKey");
        Integer raceMaxCount = (Integer) redisTemplate.opsForHash().get("match-open", "raceMaxCount");
        if (raceCountKey != null && raceMaxCount != null) {
            Integer count = (Integer) redisTemplate.opsForHash().get(raceCountKey, userid);
            if (count != null) {
                if (count >= raceMaxCount) {
                    sendMessageById(userid, "RacePlayerMaxCount");
                    return;
                }
            } else {
                sendMessageById(userid, "RacePlayerNotFound");
                return;
            }
        }


        if (redisTemplate.opsForHash().hasKey("PlayersBanned", userid)) {
            long curmill = System.currentTimeMillis()/1000;
            long banUntil = (Integer) Objects.requireNonNull(redisTemplate.opsForHash().get("PlayersBanned", userid));
            log.info(curmill + "");
            log.info(banUntil + "");
            if (curmill < banUntil) {
                sendMessageById(userid, "PlayerBanned");
                return;
            }
        }
        if (matchQueue.contains(userid)) {
            log.info(userid + "处于匹配队列中");
            return;
        }
        log.info(userid + "加入匹配队列");
        matchQueue.add(userid);
    }

    public void quitMatch(String userid) {
        if (matchQueue.remove(userid)) {
            sendMessageById(userid, "SuccessQuitMatch");
        }
    }

    /**
     * 匹配队列线程，bp网站不需要
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("Init Conventional Match Queue");
        Executors.newSingleThreadExecutor().execute(new Thread(this::executeThread));
        Executors.newSingleThreadExecutor().execute(new Thread(this::heartbeatThread));
        Executors.newSingleThreadExecutor().execute(new Thread(this::matchTimeIns));
    }

    private void executeThread() {
        while (true) {
            try {
//                log.info(matchQueue.toString());
//                LoginerWS.showSessions();
//                LoginerWS.closeSessionNotInQueue(matchQueue);
                if (matchQueue.size() >= 2) {
                    String player1 = matchQueue.take();
                    String player2 = matchQueue.take();

                    String raceCombKey = (String) redisTemplate.opsForHash().get("match-open", "raceCombKey");
                    String raceCountKey = (String) redisTemplate.opsForHash().get("match-open", "raceCountKey");
                    Integer raceMatchTimeout = (Integer) redisTemplate.opsForHash().get("match-open", "raceMatchTimeout");
                    if (raceCombKey != null && raceCountKey != null && raceMatchTimeout != null) {
                        boolean comb1 = Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(raceCombKey, player1 + "-" + player2));
                        boolean comb2 = Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(raceCombKey, player2 + "-" + player1));
                        if (comb1 || comb2) {
                            // 该2个米米号已经匹配过
                            Integer player1Time = (Integer) redisTemplate.opsForHash().get("matchTime", player1);
                            Integer player2Time = (Integer) redisTemplate.opsForHash().get("matchTime", player2);
                            if (player1Time != null && player2Time != null
                                    && player1Time > raceMatchTimeout && player2Time > raceMatchTimeout
                                    && !(comb1 && comb2)) {
                                // 2个米米号都匹配累计超过给定的时间，并且没有重复标记
                                if (comb1) {
                                    log.info("匹配player1：{}, 匹配player2：{}", player2, player1);
                                    createConventional(player2, player1, "race");
//                                    redisTemplate.opsForHash().put(raceCombKey, player2 + "-" + player1, "matched");
                                } else {
                                    log.info("匹配player1：{}, 匹配player2：{}", player1, player2);
                                    createConventional(player1, player2, "race");
//                                    redisTemplate.opsForHash().put(raceCombKey, player1 + "-" + player2, "matched");
                                }
                                redisTemplate.opsForHash().increment(raceCountKey, player1, 1L);
                                redisTemplate.opsForHash().increment(raceCountKey, player2, 1L);
                                redisTemplate.opsForHash().put("matchTime", player1, 0);
                                redisTemplate.opsForHash().put("matchTime", player2, 0);
                            } else {
                                // 2个米米号没有都匹配累计超过给定的时间，或者已经用完重复次数
                                if (LoginerWS.checkSession(player1)) {
                                    matchQueue.add(player1);
                                }
                                if (LoginerWS.checkSession(player2)) {
                                    matchQueue.add(player2);
                                }
                            }
                        } else {
                            log.info("匹配player1：{}, 匹配player2：{}", player1, player2);
                            createConventional(player1, player2, "race");
//                            redisTemplate.opsForHash().put(raceCombKey, player1 + "-" + player2, "matched");
                            redisTemplate.opsForHash().increment(raceCountKey, player1, 1L);
                            redisTemplate.opsForHash().increment(raceCountKey, player2, 1L);
                            redisTemplate.opsForHash().put("matchTime", player1, 0);
                            redisTemplate.opsForHash().put("matchTime", player2, 0);
                        }
                    } else {
                        log.info("匹配player1：{}, 匹配player2：{}", player1, player2);
                        createConventional(player1, player2, "common");
                        redisTemplate.opsForHash().put("matchTime", player1, 0);
                        redisTemplate.opsForHash().put("matchTime", player2, 0);
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void heartbeatThread() {
        while (true) {
            try {
                Thread.sleep(10000);
                heartbeatAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void matchTimeIns() {
        while (true) {
            try {
                Thread.sleep(1000);
                for (String player: matchQueue) {
                    redisTemplate.opsForHash().increment("matchTime", player, 1L);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createConventional(String player1, String player2, String conventionalMode) {
        String groupId = (String) redisTemplate.opsForHash().get("match-open", "groupId");
        if (groupId == null) return;
        HashMap<String, String> tmp = gameInformationService.generateConventionalGame(groupId, player1, conventionalMode);
        ResultUtil<String> resultObj = gameInformationService.joinConventionalGame(player2, tmp);
        sendMessageById(player1, "onMatch");
        sendMessageById(player2, "onMatch");
    }

    public List<String> getMatchPlayers() {
        return matchQueue.stream().toList();
    }
}
