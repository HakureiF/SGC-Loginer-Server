package com.seer.seerweb.component;

import com.alibaba.fastjson.JSON;
import com.seer.seerweb.entity.vo.BagPetVO;
import com.seer.seerweb.service.GameInformationService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;


/**
 * The type Delay queue manager.
 */
@Component
@Slf4j
public class DelayQueueManager implements CommandLineRunner {

  @Autowired
  GameInformationService gameInformationService;
  @Autowired
  RedisTemplate<String, String> redisTemplate;
  /**
   * -- GETTER --
   *  Gets delay queue.
   *
   */
  @Getter
  private final DelayQueue<DelayTask> delayQueue = new DelayQueue<>();

  private final Map<String, DelayTask> delayMap = new ConcurrentHashMap<>();


  /**
   * 加入到延时队列中.
   *
   * @param task the task
   */
  public void put(DelayTask task) {
    log.info("加入延时任务：{}", task);
    delayQueue.put(task);
    delayMap.put(task.data() + task.gameId(), task);
  }

  /**
   * 取消延时任务.
   *
   * @param taskId the task id
   * @param gameId the game id
   * @return boolean the boolean
   */
  public void remove(String taskId, String gameId) {
    log.info("取消延时任务：{}, {}", taskId, gameId);
    if (delayMap.containsKey(taskId + gameId)) {
      delayMap.get(taskId + gameId).clear();
      delayMap.remove(taskId + gameId);
    }
  }

  public void remove(DelayTask task) {
    log.info("取消延时任务：{}, {}", task.data(), task.gameId());
    if (delayMap.containsKey(task.data() + task.gameId())) {
      delayMap.get(task.data() + task.gameId()).clear();
      delayMap.remove(task.data() + task.gameId());
    }
  }

  @Override
  public void run(String... args) {
    log.info("Init Delayed Queue");
    Executors.newSingleThreadExecutor().execute(new Thread(this::executeThread));
  }

  /**
   * 延时任务执行线程.
   */
  private void executeThread() {
    while (true) {
      try {
        DelayTask task = delayQueue.take();
        processTask(task);
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  private void processTask(DelayTask task) {
    log.info("执行延时任务：{}", task.toString());
    String gameId = task.gameId();
    String phase = task.data();
    if (phase.contains("BanElf")) {
      handleConventionalBan(gameId);
    }
    if (phase.contains("PickElf")) {
      if (phase.contains("First")) {
        handleConventionalPickFirst(gameId);
      } else {
        handleConventionalPickRemain(gameId);
      }
    }
    if (phase.startsWith("MatchOffLine")) {
      handleMatchOffline(gameId, phase);
    }
  }

  private void handleConventionalBan(String gameId){
    String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
    String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
    String phase = (String) redisTemplate.opsForHash().get(gameId, "phase");
    if (phase == null || !phase.contains("BanElf")) {
      return;
    }
    redisTemplate.opsForHash().put(gameId, "phase", "PlayerPickElfFirst");
    randomConventionalBan(gameId);
    LoginerWS.sendMessageById(player1, "PlayerPickElfFirst");
    LoginerWS.sendMessageById(player2, "PlayerPickElfFirst");
    taskConventionalPickFirst(gameId, player1, player2);
  }

  private void randomConventionalBan(String gameId) {
    Random random = new Random();
    String player1PetStateStr = (String) redisTemplate.opsForHash().get(gameId, "Player1PetState");
    String player2PetStateStr = (String) redisTemplate.opsForHash().get(gameId, "Player2PetState");
    List<BagPetVO> player1PetState = JSON.parseArray(player1PetStateStr, BagPetVO.class);
    List<BagPetVO> player2PetState = JSON.parseArray(player2PetStateStr, BagPetVO.class);
    if (player1PetState != null && !player1PetStateStr.contains("\"state\":1")) {
      Set<Integer> banindex = new HashSet<>();
      while (banindex.size() < 3) {
        int randomNumber = random.nextInt(player1PetState.size());
        banindex.add(randomNumber);
      }
      for (Integer i: banindex) {
        player1PetState.get(i).setState(1);
      }
      redisTemplate.opsForHash().put(gameId, "Player1PetState", JSON.toJSONString(player1PetState));
    }
    if (player2PetState != null && !player2PetStateStr.contains("\"state\":1")) {
      Set<Integer> banindex = new HashSet<>();
      while (banindex.size() < 3) {
        int randomNumber = random.nextInt(player2PetState.size());
        banindex.add(randomNumber);
      }
      for (Integer i: banindex) {
        player2PetState.get(i).setState(1);
      }
      redisTemplate.opsForHash().put(gameId, "Player2PetState", JSON.toJSONString(player2PetState));
    }
  }

  private void handleConventionalPickFirst(String gameId) {
    String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
    String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
    String phase = (String) redisTemplate.opsForHash().get(gameId, "phase");
    if (phase == null || !phase.contains("ElfFirst")) {
      return;
    }
    redisTemplate.opsForHash().put(gameId, "phase", "PlayerPickElfRemain");
    randomConventionalPickFirst(gameId);
    LoginerWS.sendMessageById(player1, "PlayerPickElfRemain");
    LoginerWS.sendMessageById(player2, "PlayerPickElfRemain");
    taskConventionalPickRemain(gameId, player1, player2);
  }

  private void randomConventionalPickFirst(String gameId) {
    Random random = new Random();
    String player1PetStateStr = (String) redisTemplate.opsForHash().get(gameId, "Player1PetState");
    String player2PetStateStr = (String) redisTemplate.opsForHash().get(gameId, "Player2PetState");
    List<BagPetVO> player1PetState = JSON.parseArray(player1PetStateStr, BagPetVO.class);
    List<BagPetVO> player2PetState = JSON.parseArray(player2PetStateStr, BagPetVO.class);
    if (player1PetState != null && !player1PetStateStr.contains("\"state\":2")) {
      int index = random.nextInt(player1PetState.size());
      while (player1PetState.get(index).getState() != 0) {
        index = random.nextInt(player1PetState.size());
      }
      player1PetState.get(index).setState(2);
      redisTemplate.opsForHash().put(gameId, "Player1PetState", JSON.toJSONString(player1PetState));
    }
    if (player2PetState != null && !player2PetStateStr.contains("\"state\":2")) {
      int index = random.nextInt(player2PetState.size());
      while (player2PetState.get(index).getState() != 0) {
        index = random.nextInt(player2PetState.size());
      }
      player2PetState.get(index).setState(2);
      redisTemplate.opsForHash().put(gameId, "Player2PetState", JSON.toJSONString(player2PetState));
    }
  }

  private void handleConventionalPickRemain(String gameId) {
    String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
    String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
    String phase = (String) redisTemplate.opsForHash().get(gameId, "phase");
    if (phase == null || !phase.contains("ElfRemain")) {
      return;
    }
    redisTemplate.opsForHash().put(gameId, "phase", "WaitingPeriodResult");
    randomConventionalPickRemain(gameId);
    LoginerWS.sendMessageById(player1, "WaitingPeriodResult");
    LoginerWS.sendMessageById(player2, "WaitingPeriodResult");
  }

  private void randomConventionalPickRemain(String gameId) {
    Random random = new Random();
    String player1PetStateStr = (String) redisTemplate.opsForHash().get(gameId, "Player1PetState");
    String player2PetStateStr = (String) redisTemplate.opsForHash().get(gameId, "Player2PetState");
    List<BagPetVO> player1PetState = JSON.parseArray(player1PetStateStr, BagPetVO.class);
    List<BagPetVO> player2PetState = JSON.parseArray(player2PetStateStr, BagPetVO.class);

    if (player1PetState != null && !player1PetStateStr.contains("\"state\":3")) {
      for (int i=0; i<5; i++) {
        int index = random.nextInt(player1PetState.size());
        while (player1PetState.get(index).getState() != 0) {
          index = random.nextInt(player1PetState.size());
        }
        player1PetState.get(index).setState(3);
      }
      redisTemplate.opsForHash().put(gameId, "Player1PetState", JSON.toJSONString(player1PetState));
    }
    if (player2PetState != null && !player2PetStateStr.contains("\"state\":3")) {
      for (int i=0; i<5; i++) {
        int index = random.nextInt(player2PetState.size());
        while (player2PetState.get(index).getState() != 0) {
          index = random.nextInt(player2PetState.size());
        }
        player2PetState.get(index).setState(3);
      }
      redisTemplate.opsForHash().put(gameId, "Player2PetState", JSON.toJSONString(player2PetState));
    }
  }

  public void handleMatchOffline(String gameId, String phase) {
    if (gameId != null) {
      String userId = phase.substring(12); //掉线人id
      String player1 = (String) redisTemplate.opsForHash().get(gameId, "Player1");
      String player2 = (String) redisTemplate.opsForHash().get(gameId, "Player2");
      if (player1 != null && player1.equals(userId)) {
        // 蓝方掉线
        redisTemplate.opsForHash().put(gameId, "fightResult", "player2Offline");
        gameInformationService.exitGame("game" + player1);
      }
      if (player2 != null&& player2.equals(userId)) {
        // 红方掉线
        redisTemplate.opsForHash().put(gameId, "fightResult", "player2Offline");
        gameInformationService.exitGame("game" + player2);
      }
//      LoginerWS.sendMessageById(player1, "offLine");
//      LoginerWS.sendMessageById(player2, "offLine");
    }
  }


  private Integer getIntervalTime(String gameId) {
    // normal是常规bp，pool是时间池
    Integer gameInterval = (Integer) redisTemplate.opsForHash().get(gameId,"BPTime");
    if (gameInterval == null) {
      log.info("查询不到时间");
      return 0;
    }
    if (gameInterval < 10) {
      gameInterval = 10;
    }
    return gameInterval;
  }

  /**
   *
   * @param gameId
   * @param player1
   * @param player2
   */
  public void taskConventionalBan(String gameId, String player1, String player2) {
    Integer banCountTime = (Integer) redisTemplate.opsForHash().get(gameId, "banCountTime");
    if (banCountTime == null) {
      banCountTime = 30;
    }
    DelayTask delayTask = new DelayTask("PlayerBanElf", (banCountTime + 1) * 1000L, gameId);
    put(delayTask);
  }


  public void taskConventionalPickFirst(String gameId, String player1, String player2) {
    Integer firstCountTime = (Integer) redisTemplate.opsForHash().get(gameId, "firstCountTime");
    if (firstCountTime == null) {
      firstCountTime = 30;
    }
    DelayTask delayTask = new DelayTask("PlayerPickElfFirst", (firstCountTime + 1) * 1000L, gameId);
    put(delayTask);
  }

  public void taskConventionalPickRemain(String gameId, String player1, String player2) {
    Integer remainCountTime = (Integer) redisTemplate.opsForHash().get(gameId, "remainCountTime");
    if (remainCountTime == null) {
      remainCountTime = 30;
    }
    DelayTask delayTask = new DelayTask("PlayerPickElfRemain", (remainCountTime + 1) * 1000L, gameId);
    put(delayTask);
  }

  public void taskMatchOffline(String gameId, String userId) {
    DelayTask delayTask = new DelayTask("MatchOffLine" + userId, 10 * 1000L, gameId);
    put(delayTask);
  }
}
