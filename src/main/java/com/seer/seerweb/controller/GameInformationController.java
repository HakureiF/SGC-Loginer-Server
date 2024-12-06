package com.seer.seerweb.controller;

import com.seer.seerweb.annotation.AccessLimit;
import com.seer.seerweb.component.DelayQueueManager;
import com.seer.seerweb.component.DelayTask;
import com.seer.seerweb.component.LoginerWS;
import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.service.GameInformationService;
import com.seer.seerweb.service.SeerSuitService;
import com.seer.seerweb.service.UserInformationService;
import com.seer.seerweb.utils.AesUtil;
import com.seer.seerweb.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 前端控制器.
 *
 * @author chen
 * @since 2023 -04-23
 */
@RestController
@RequestMapping("/api/game-information")
public class GameInformationController {
  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  /**
   * The Config content.
   */
  @Autowired
  ConfigContent configContent;
  /**
   * The Game information service.
   */
  @Autowired
  GameInformationService gameInformationService;
  /**
   * The User information service.
   */
  @Autowired
  UserInformationService userInformationService;
  /**
   * The Seer suit service.
   */
  @Autowired
  SeerSuitService seerSuitService;
  /**
   * The Delay queue manager.
   */
  @Autowired
  DelayQueueManager delayQueueManager;

  @GetMapping("/setConventionalSuit")
  @ResponseBody
  public ResultUtil<String> setConventionalSuit(@RequestParam("suitId") Integer suitId, @RequestHeader("seer-userid")String id){
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    return gameInformationService.setConventionalSuit(userid, suitId);
  }

//  @PostMapping("/generateConventionalGame")
  @GetMapping("/generateConventionalGame")
  @ResponseBody
//  public ResultUtil<HashMap<String, String>> generateConventionalGame(@RequestBody HashMap<String, Object> option,
  public ResultUtil<HashMap<String, String>> generateConventionalGame(@RequestParam("groupId") String groupId,
                                                          @RequestHeader("seer-userid")String userid) {
    // option中应该定义bo数与比赛组
    String id = AesUtil.decrypt(userid, configContent.getAesKey());
    //首先查看用户是否处于对战状态
    if (Boolean.TRUE.equals(redisTemplate.hasKey("game" + id))) {
      return ResultUtil.fail("创建失败，您已经加入了一场比赛", null);
    } else {
      // 设置用户对战状态
//      HashMap<String, String> hashMap = gameInformationService.generateConventionalGame(option, id);
      HashMap<String, String> hashMap = gameInformationService.generateConventionalGame(groupId, id, "common", false);
      if (hashMap != null) {
        return ResultUtil.success(hashMap);
      } else {
        return ResultUtil.fail("传入参数有误", null);
      }
    }
  }
  /**
   * Exit game result util.
   *
   * @param userid the userid
   * @return the result util
   */
  @PostMapping ("/joinConventionalGame")
  @ResponseBody
  public ResultUtil<String> joinConventionalGame(@RequestHeader("seer-userid")String userid, @RequestBody HashMap<String,String> gameInfo) {
    String id = AesUtil.decrypt(userid, configContent.getAesKey());
    return gameInformationService.joinConventionalGame(id, gameInfo);
  }

  /**
   * Exit game result util.
   *
   * @param userid the userid
   * @return the result util
   */
  @GetMapping ("/exitGame")
  @ResponseBody
  public ResultUtil<String> exitGame(@RequestHeader("seer-userid")String userid) {
    String id = "game" +  AesUtil.decrypt(userid, configContent.getAesKey());
    return gameInformationService.exitGame(id);
  }

  /**
   * Game state result util.
   *
   * @param userid the userid
   * @return the result util
   */
  @GetMapping("/gameState")
  @ResponseBody
  public ResultUtil<String> gameState(@RequestHeader("seer-userid") String userid) {
    String id = "game" +  AesUtil.decrypt(userid, configContent.getAesKey());
    if (Boolean.TRUE.equals(redisTemplate.hasKey(id))) {
      String gameMode = (String) redisTemplate.opsForHash().get(id, "GameMode");
      if (gameMode == null) {
        return ResultUtil.fail(404, "查询异常", "");
      }
      if (gameMode.equals("conventional")) {
        return ResultUtil.success(200, "请在登录器上使用", "");
      } else {
        return ResultUtil.success(201, "","");
      }
    }
    return ResultUtil.fail(202, "您当前未加入任何比赛", "");
  }

  /**
   * Gets type.
   *
   * @param userid the userid
   * @return the type
   */
  @GetMapping("/getType")
  @ResponseBody
  public ResultUtil<String> getType(@RequestHeader("seer-userid") String userid) {
    String id =  "game" + AesUtil.decrypt(userid, configContent.getAesKey());
    if (Boolean.TRUE.equals(redisTemplate.hasKey(id))) {
      return ResultUtil.success((String) redisTemplate.opsForHash().get(id, "type"));
    } else {
      return ResultUtil.fail("您尚未加入任何比赛", null);
    }
  }

  /**
   * Gets phase.
   *
   * @param id the userid
   * @return the phase
   */
  @GetMapping("/getPhase")
  @ResponseBody
  public ResultUtil<String> getPhase(@RequestHeader("seer-userid") String id) {
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
    if (gameId != null) {
      return ResultUtil.success((String) redisTemplate.opsForHash().get(gameId, "phase"));
    }
    return ResultUtil.fail("您尚未选择身份", null);
  }

  /**
   * Gets players.
   *
   * @param id the userid
   * @return the players
   */
  @GetMapping("/getPlayers")
  @ResponseBody
  public ResultUtil<HashMap<String, String>> getPlayers(@RequestHeader("seer-userid") String id) {
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
    if (gameId != null) {
      HashMap<String, String> hashMap = new HashMap<>();
      String player1Userid =  (String) redisTemplate.opsForHash().get(gameId, "Player1");
      String player2Userid =  (String) redisTemplate.opsForHash().get(gameId, "Player2");
      if (player1Userid != null && player1Userid.contains("seeraccount")) {
        hashMap.put("Player1", player1Userid.replace("seeraccount", ""));
      } else {
        hashMap.put("Player1", userInformationService.getNickNameById(player1Userid));
      }
      if (player2Userid != null && player2Userid.contains("seeraccount")){
        hashMap.put("Player2", player2Userid.replace("seeraccount", ""));
      } else {
        hashMap.put("Player2", userInformationService.getNickNameById(player2Userid));
      }
      return ResultUtil.success(hashMap);
    } else {
      return ResultUtil.fail("您尚未选择身份", null);
    }
  }

  /**
   * Gets count time.
   *
   * @param id the id
   * @return the count time
   */
  @GetMapping("/getCountTime")
  @ResponseBody
  public ResultUtil<Long> getCountTime(@RequestHeader("seer-userid") String id) {
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");

    if (gameId != null) {
      String phase = (String) redisTemplate.opsForHash().get(gameId, "phase");

      for (DelayTask delayTask : delayQueueManager.getDelayQueue()) {
        if (delayTask.gameId().equals(gameId) && delayTask.data().equals(phase)) {
          return ResultUtil.success(delayTask.getDelay(TimeUnit.SECONDS) / 1000);
        }
      }
    }
    return ResultUtil.success("当前不处于倒计时阶段", 0L);
  }

  /**
   * Gets pick suit.
   *
   * @param id the id
   * @return the picked suit
   */
  @GetMapping("/getPickSuit")
  @ResponseBody
  public ResultUtil<HashMap<String, String>> getPickSuit(@RequestHeader("seer-userid") String id) {
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    HashMap<String, String> hashMap = seerSuitService.getSuit(userid, "Pick");
    if (hashMap != null) {
      return ResultUtil.success(hashMap);
    }
    return ResultUtil.fail("套装获取错误", null);
  }

  /**
   * Gets player 1 pick list.
   *
   * @param id the id
   * @return the player 1 pick list
   */
  @GetMapping("/getPlayer1PickList")
  @ResponseBody
  public ResultUtil<List<String>> getPlayer1PickList(@RequestHeader("seer-userid") String id) {
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    if (userid != null) {
      String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
      String Player1 = "";
      if (gameId != null) {
        Player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
        if (Objects.equals(redisTemplate.opsForHash().get(gameId, "GameMode"), "Conventional")){
          // 123模式下校验是否本人请求
          if (!Objects.equals(userid, Player1)){
            return ResultUtil.fail("非本人请求", null);
          }
        }
      }
      List<String> list = gameInformationService.getPickList(userid, gameId, Player1);
      if (list != null) {
        return ResultUtil.success(list);
      }
    }
    return ResultUtil.fail("您尚未选择身份", null);
  }

  /**
   * Gets player 2 pick list.
   *
   * @param id the id
   * @return the player 2 pick list
   */
  @GetMapping("/getPlayer2PickList")
  @ResponseBody
  public ResultUtil<List<String>> getPlayer2PickList(@RequestHeader("seer-userid") String id) {
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    if (userid != null) {
      String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
      String Player2 = "";
      if (gameId != null) {
        Player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
        if (Objects.equals(redisTemplate.opsForHash().get(gameId, "GameMode"), "Conventional")){
          // 123模式下校验是否本人请求
          if (!Objects.equals(userid, Player2)){
            return ResultUtil.fail("非本人请求", null);
          }
        }
      }
      List<String> list = gameInformationService.getPickList(userid, gameId, Player2);
      if (list != null) {
        return ResultUtil.success(list);
      }
    }
    return ResultUtil.fail("您尚未选择身份", null);
  }

  /**
   * 获取房间号
   * @param id
   * @return
   */
  @GetMapping("/getRoomId")
  @ResponseBody
  public ResultUtil<Integer> getRoomId(@RequestHeader("seer-userid") String id){
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
    if (gameId != null){
      if(Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(gameId, "RoomId"))){
        return ResultUtil.success((Integer) redisTemplate.opsForHash().get(gameId, "RoomId"));
      }
    }
    return ResultUtil.fail("没有找到房间号", null);
  }

  /**
   * 获取双方米米号（未启用）
   * @param id
   * @return
   */
  public ResultUtil<HashMap<String, String>> getMimiId(@RequestHeader("seer-userid") String id){
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    try {
      return ResultUtil.success(gameInformationService.getDoubleGameHash(userid, "mimiId"));
    } catch (Exception e){
      e.printStackTrace();
      return ResultUtil.fail("刷新背包失败", null);
    }
  }


  /**
   * 清除意外情况导致的个人对局数据残留
   * @param id
   * @return
   */
  @GetMapping("/removeGameCache")
  @ResponseBody
  @AccessLimit(seconds = 2, maxCount = 20)
  public ResultUtil<String> removeGameCache(@RequestHeader("seer-userid") String id) {
    String userid = AesUtil.decrypt(id, configContent.getAesKey());
    LoginerWS.removeSession(userid);
    String gameId = (String) redisTemplate.opsForHash().get("game" + userid, "gameId");
    if (gameId != null) {
      if (Objects.equals(redisTemplate.opsForHash().get(gameId, "conventionalMode"), "common")) {
        redisTemplate.delete(gameId);
      }
      redisTemplate.delete("PickElfList" + userid);
      redisTemplate.delete("BanElfList" + userid);
      redisTemplate.delete("game" + userid);
    }
    return ResultUtil.success();
  }
}
