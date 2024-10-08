package com.seer.seerweb.service.impl;

import com.alibaba.fastjson.JSON;
import com.seer.seerweb.component.LoginerWS;
import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.entity.Racegroup;
import com.seer.seerweb.entity.vo.BagPetVO;
import com.seer.seerweb.entity.vo.VerifySuitVO;
import com.seer.seerweb.service.ConventionalService;
import com.seer.seerweb.service.GameInformationService;
import com.seer.seerweb.service.RacegroupService;
import com.seer.seerweb.utils.ResultUtil;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
* @author Glory
* {@code @description} 针对表【GameInformation】的数据库操作Service实现
* {@code @createDate} 2023-06-22 22:59:49
 */
@Service
@Log
public class GameInformationServiceImpl implements GameInformationService{
  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  ConfigContent configContent;
  @Autowired
  RacegroupService racegroupService;
  @Autowired
  ConventionalService conventionalService;


  /**
   * @param userid
   * @return
   */
  @Override
//  public HashMap<String, String> generateConventionalGame(HashMap<String, Object> option, String userid) {
  public HashMap<String, String> generateConventionalGame(String groupId, String userid, String conventionalMode) {
    if (Boolean.TRUE.equals(redisTemplate.hasKey("game" + userid))) {
      return null;
    }
    StringBuilder gameId = new StringBuilder(11);
    Random random = new Random();

    do {
      gameId.delete(0,gameId.length());
      //生成随机对局号
      for (int i =0; i < 16 ; ++i) {
        int nextInt = random.nextInt(10);
        gameId.append(nextInt);
      }
//    } while (existId(gameId.toString()));
    } while (Boolean.TRUE.equals(redisTemplate.hasKey("game" + gameId.toString())));

    String id = gameId.toString();
    Timestamp timeStamp = new Timestamp(new Date().getTime());
    String GameMode = "Conventional";
    Racegroup racegroup = racegroupService.getById(groupId);
    String EnableOfficialBan = racegroup.getEnableBan();
    String EnableElfPool = racegroup.getEnablePool();
    String OfficialBan = "[]";
    if(EnableOfficialBan.equals("enable")){
      OfficialBan = racegroup.getBanElfList();
    }

    // 创建新对局,房主为建房人
    redisTemplate.opsForHash().put("game" + id,"Judge",userid);
    redisTemplate.opsForHash().put("game" + userid,"gameId","game" + id);
    redisTemplate.opsForHash().put("game" + id,"Player1",userid);
    redisTemplate.opsForHash().put("game" + id,"Player2","");
    redisTemplate.opsForHash().put("game" + userid,"type","Player1");
    redisTemplate.opsForHash().put("game" + userid,"WinCount",0);
    redisTemplate.opsForHash().put("game" + id,"conventionalMode", conventionalMode);

    redisTemplate.opsForHash().put("game" + id,"num",1);
    redisTemplate.opsForHash().put("game" + id,"CurrentPeriod",1);
    redisTemplate.opsForHash().put("game" + id,"GameMode",GameMode);
    redisTemplate.opsForHash().put("game" + id,"EnableOfficialBan",EnableOfficialBan);
    redisTemplate.opsForHash().put("game" + id,"EnableElfPool",EnableElfPool);

    redisTemplate.opsForHash().put("game" + id,"PeriodNum","1");
    redisTemplate.opsForHash().put("game" + id,"phase","WaitingStage");
    redisTemplate.opsForHash().put("game" + id,"OfficialBan", OfficialBan);
    redisTemplate.opsForHash().put("game" + id,"groupId", racegroup.getGroupId());
    redisTemplate.opsForHash().put("game" + id, "banNum", racegroup.getBanNum());
    redisTemplate.opsForHash().put("game" + id, "banCountTime", racegroup.getBanCountTime());
    redisTemplate.opsForHash().put("game" + id, "firstCountTime", racegroup.getFirstCountTime());
    redisTemplate.opsForHash().put("game" + id, "remainCountTime", racegroup.getRemainCountTime());
    if(EnableElfPool.equals("enable")){
      if(Boolean.TRUE.equals(redisTemplate.hasKey("Group" + racegroup.getGroupId() + "ElfPool"))){ // 检查redis中有无精灵池，没有则添加
        redisTemplate.expire("Group" + racegroup.getGroupId() + "ElfPool", 24, TimeUnit.HOURS);
      } else {
        racegroupService.initElfPool(racegroup.getGroupId(), racegroup.getPickElfList());
      }
    }
    racegroupService.initLimitPool(racegroup.getGroupId(), racegroup.getLimitPool(), racegroup.getAwardPool(), racegroup.getPunishPool());
    // 设置过期时间
    redisTemplate.expire("game" + id,configContent.getGameTime(), TimeUnit.HOURS);
    redisTemplate.expire("game" + userid,configContent.getGameTime(),TimeUnit.HOURS);

    HashMap<String,String> hashMap = new HashMap<>();
    hashMap.put("gameId",id);
    return hashMap;
  }
  /**
   * @param id userid
   * @param gameInfo the gameInfo include gameId and code generated on loginer
   * @return
   */
  @Override
  public ResultUtil<String> joinConventionalGame(String id, HashMap<String, String> gameInfo) {
    String gameId = "game" + gameInfo.get("gameId");
    // 判断是否存在对局
    if (!Boolean.TRUE.equals(redisTemplate.hasKey(gameId))) {
      log.info("不存在对局");
      return ResultUtil.fail("不存在对局",null);
    }
    if (!Objects.equals(redisTemplate.opsForHash().get(gameId, "GameMode"), "Conventional")) {
      log.info("非12ban3模式，请在网页加入");
      return ResultUtil.fail("非12ban3模式，请在网页加入",null);
    }
    Object o = redisTemplate.opsForHash().get(gameId, "num");
    int num;
    if (o != null) {
      num = (int) o;
    } else {
      log.info("人数错误");
      return ResultUtil.fail("人数错误",null);
    }
    if (num == 2) {
      log.info("人数已满");
      return ResultUtil.fail("人数已满",null);
    }
    // 加入redis
    redisTemplate.opsForHash().put("game" + id, "type", "Player2");
    redisTemplate.opsForHash().put(gameId,"Player2",id);
    redisTemplate.opsForHash().put("game" + id,"WinCount",0);
    redisTemplate.opsForHash().put("game" + id,"gameId",gameId);
    redisTemplate.expire("game" + id,configContent.getGameTime(),TimeUnit.HOURS);
    redisTemplate.opsForHash().put(gameId,"num", ++num);
    redisTemplate.opsForHash().put(gameId, "fightResult", "matched");
    if (Objects.equals(redisTemplate.opsForHash().get(gameId, "conventionalMode"), "race")) {
      String raceCombKey = (String) redisTemplate.opsForHash().get("match-open", "raceCombKey");
      if (raceCombKey != null) {
        redisTemplate.opsForHash().put(raceCombKey, redisTemplate.opsForHash().get(gameId, "Player1") + "-" + id, gameId);
      }
    }
    if (!Objects.equals(redisTemplate.opsForHash().get(gameId, "Player1"), "")
        && !Objects.equals(redisTemplate.opsForHash().get(gameId, "Player2"), "")
    ) {
      // 人满时进入准备阶段
      redisTemplate.opsForHash().put(gameId,"phase", "PreparationStage");
      LoginerWS.sendMessageByGameId("All members are present",
              (String) redisTemplate.opsForHash().get(gameId, "Player1"),
              (String) redisTemplate.opsForHash().get(gameId, "Player2"));
    }
    return ResultUtil.success();
  }

  /**
   * @param userid
   * @return
   */
  @Override
  public List<String> getPickList(String userid, String gameId, String playerId) {
    BoundListOperations<String, String> listOperations = redisTemplate.boundListOps("PickElfList" + playerId);
    BoundHashOperations<String, String, Object> hashOperations = redisTemplate.boundHashOps(gameId);
    String gameMode = Objects.requireNonNull(hashOperations.get("GameMode")).toString();
    String judge = Objects.requireNonNull(hashOperations.get("Judge")).toString();
    List<String> list = listOperations.range(0, -1);
//    int size = 0;
//    if (list != null) {
//      size = list.size();
//    }
//    if (Objects.equals(redisTemplate.opsForHash().get(gameId, "HideLastElf"), "enable")
//        && size == 6
//        && !userid.equals(playerId)
//    ) {
//      if ((gameMode.equals("senior") && !userid.equals(judge)) || gameMode.equals("junior")) {
//        size = 5;
//      }
//    }
//    return listOperations.range(0, size - 1);

    // 不是本人请求，以及不是senior局的裁判请求就需要保护暗手
    if (list != null && !userid.equals(playerId) && ((gameMode.equals("senior") && !userid.equals(judge)) || gameMode.equals("junior"))) {
      String hidePick = (String) redisTemplate.opsForHash().get(gameId, "HidePick");
      if (hidePick != null) {
        for (int i = 1; i < list.size() + 1; i++) {
          if (hidePick.charAt(i) != '0') {
            list.set(i-1, "1");
          }
        }
      }
    }
    return list;
  }

  /**
   * @param id userid
   * @return Result
   */
  @Override
  public ResultUtil<String> exitGame(String id) {
    //首先查看用户是否处于对战状态
    if (Boolean.TRUE.equals(redisTemplate.hasKey(id))) {
      String type = (String) redisTemplate.opsForHash().get(id,"type");
      if (type != null && type.equals("Watcher")) {
        redisTemplate.delete(id);
        return ResultUtil.success();
      }
      if (type != null && type.equals("Judge")) {
        return ResultUtil.fail("退出失败，房主无法退出比赛", "");
      }
      String gameId = (String) redisTemplate.opsForHash().get(id,"gameId");
      if (gameId == null) {
        if (id.contains("seeraccount")) {
          redisTemplate.delete("PickElfList" + id.substring(4));
          redisTemplate.delete("BanElfList" + id.substring(4));
          redisTemplate.delete(id);
        }
        return ResultUtil.success();
      }
      String phase = (String) redisTemplate.opsForHash().get(gameId,"phase");
      String Player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
      String Player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
      if (Objects.equals(redisTemplate.opsForHash().get(gameId, "GameMode"), "Conventional")){
        // 12ban3模式
        if (id.contains("seeraccount")) {
          // 匹配模式
          String groupId = (String) redisTemplate.opsForHash().get(gameId, "groupId");
          if (Player1 != null && Player2 != null && id.contains(Player1)) {
            // 蓝方退出，红方加分
            String mimiId = Player2.substring(11);
            int score = 15;
            String mypetStateStr = (String) redisTemplate.opsForHash().get(gameId, "Player2PetState");
            if (mypetStateStr != null) {
              log.info("红方加分");
              List<BagPetVO> mypetState = JSON.parseArray(mypetStateStr, BagPetVO.class);
              score -= conventionalService.scoreReduced(groupId, mypetState, true);
              if (Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey("ScoreBoard" + groupId, mimiId))) {
                redisTemplate.opsForHash().increment("ScoreBoard" + groupId, mimiId, score);
              } else {
                redisTemplate.opsForHash().put("ScoreBoard" + groupId, mimiId, 1000 + score);
              }
              redisTemplate.delete("game" + Player1);
              redisTemplate.delete("game" + Player2);
              if (Objects.equals(redisTemplate.opsForHash().get(gameId, "conventionalMode"), "common")) {
                redisTemplate.delete(gameId);
              }
              LoginerWS.sendMessageById(Player2, "offLine");
            }
          } else if (Player1 != null && Player2 != null && id.contains(Player2)) {
            // 红方退出，蓝方加分
            String mimiId = Player1.substring(11);
            int score = 15;
            String mypetStateStr = (String) redisTemplate.opsForHash().get(gameId, "Player1PetState");
            if (mypetStateStr != null) {
              log.info("蓝方加分");
              List<BagPetVO> mypetState = JSON.parseArray(mypetStateStr, BagPetVO.class);
              score -= conventionalService.scoreReduced(groupId, mypetState, true);
              if (Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey("ScoreBoard" + groupId, mimiId))) {
                redisTemplate.opsForHash().increment("ScoreBoard" + groupId, mimiId, score);
              } else {
                redisTemplate.opsForHash().put("ScoreBoard" + groupId, mimiId, 1000 + score);
              }
              redisTemplate.delete("game" + Player1);
              redisTemplate.delete("game" + Player2);
              if (Objects.equals(redisTemplate.opsForHash().get(gameId, "conventionalMode"), "common")) {
                redisTemplate.delete(gameId);
              }
              LoginerWS.sendMessageById(Player1, "offLine");
            }
          }
          return ResultUtil.success();
//        } else if (phase !=null && (phase.equals("WaitingStage") || phase.equals("PreparationStage")) || phase.equals("ReadyStage") || phase.equals("WaitingPeriodResult")){
        } else {
          // 房间模式
          if (type != null && type.equals("Player1")) {
            // 房主退出直接关闭房间
            LoginerWS.sendMessageById(Player2, "endGame");
            redisTemplate.delete("PickElfList" + Player1);
            redisTemplate.delete("PickElfList" + Player2);
            redisTemplate.delete("BanElfList" + Player1);
            redisTemplate.delete("BanElfList" + Player2);
            redisTemplate.delete("game" + Player1);
            redisTemplate.delete("game" + Player2);
            if (Objects.equals(redisTemplate.opsForHash().get(gameId, "conventionalMode"), "common")) {
              redisTemplate.delete(gameId);
            }
            return ResultUtil.success();
          } else {
            redisTemplate.opsForHash().put(gameId, type, "");
            redisTemplate.delete(id);
            Object o = redisTemplate.opsForHash().get(gameId, "num");
            int num = 0; if (o != null) num = (int) o;
            redisTemplate.opsForHash().put(gameId, "num", --num);
            redisTemplate.opsForHash().put(gameId,"phase","WaitingStage");
            redisTemplate.delete("PickElfList" + Player1);
            redisTemplate.delete("PickElfList" + Player2);
            redisTemplate.delete("BanElfList" + Player1);
            redisTemplate.delete("BanElfList" + Player2);
            redisTemplate.delete("game" + Player2);
            LoginerWS.sendMessageById(Player1, "Player1Exit");
            return ResultUtil.success();
          }
        }
      }else{
        Integer currentPeriod = (Integer) redisTemplate.opsForHash().get(gameId, "CurrentPeriod");
        if (phase !=null && (phase.equals("WaitingStage") || phase.equals("PreparationStage")) && currentPeriod != null && currentPeriod == 1) {
          // 退出redis
          if (type != null) {
            redisTemplate.opsForHash().put(gameId, type, "");
            redisTemplate.delete(id);
          }
          Object o = redisTemplate.opsForHash().get(gameId, "num");
          //人数减一
          int num = 0;
          if (o != null) {
            num = (int) o;
          }
          redisTemplate.opsForHash().put(gameId, "num", --num);
          redisTemplate.opsForHash().put(gameId,"phase","WaitingStage");
          return ResultUtil.success();
        }else {
          return ResultUtil.fail("退出失败，比赛已经开始", "");
        }
      }
    } else {
      return ResultUtil.fail("退出失败，您不在对战状态", "");
    }
  }


  @Override
  public ResultUtil<String> setConventionalSuit(String userid, Integer suitId) {
    String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
    if (gameId != null) {
      if (Objects.equals(redisTemplate.opsForHash().get(gameId, "phase"), "PreparationStage") ||
              Objects.equals(redisTemplate.opsForHash().get(gameId, "phase"), "WaitingStage") ||
              (Objects.equals(redisTemplate.opsForHash().get(gameId, "phase"), "ReadyStage") && Objects.equals(redisTemplate.opsForHash().get(gameId, "Player1"), userid))){
        VerifySuitVO suitVO = new VerifySuitVO();
        suitVO.setGameId(gameId);
        suitVO.setSuitId(suitId);
        String mess = conventionalService.verifySuit(suitVO);
        if (!mess.isBlank()) {
          return ResultUtil.fail(mess, null);
        }
        redisTemplate.opsForHash().put("game" + userid, "PickSuit", suitVO.getSuitId());
        return ResultUtil.success();
      }
    }
    return ResultUtil.fail();
  }

  /**
   * 获取比赛中红蓝双方的指定内容
   * @param userid
   * @param key
   * @return
   */
  @Override
  public HashMap<String, String> getDoubleGameHash(String userid, String key) {
    String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
    if (gameId != null) {
      String player1Value = (String) redisTemplate.opsForHash().get(gameId, "Player1" + key);
      String player2Value = (String) redisTemplate.opsForHash().get(gameId, "Player2" + key);
      HashMap<String, String> map = new HashMap<>();
      map.put("Player1" + key, player1Value);
      map.put("Player2" + key, player2Value);
      return map;
    }
    return null;
  }
}




