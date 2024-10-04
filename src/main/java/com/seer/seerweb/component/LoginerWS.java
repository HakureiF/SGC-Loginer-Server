package com.seer.seerweb.component;

import com.seer.seerweb.config.ConfigContent;
import com.seer.seerweb.utils.AesUtil;
import com.seer.seerweb.utils.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class LoginerWS implements WebSocketHandler {
  private static final ConcurrentHashMap<String, WebSocketSession> loginerSessions = new ConcurrentHashMap<>();
  @Autowired
  TokenGenerator tokenGenerator;
  @Autowired
  ConventionalMode conventionalMode;
  @Autowired
  ConfigContent configContent;
  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Override
  public void afterConnectionEstablished(@NotNull WebSocketSession session) {
    String userid = session.getAttributes().get("userid").toString();
    try {
      conventionalMode.reconnectMatch(userid);
      loginerSessions.put(userid, session);
      log.info("用户" + userid + "登录登录器");
      String token = tokenGenerator.generator(userid, "loginer");
      Thread.sleep(500);
      sendMessageById(userid, "token:" + AesUtil.encrypt(token,configContent.getAesKey()));
      String gameInfo = checkGameInformation(userid);
      if(gameInfo != null) {
        if (gameInfo.equals("Conventional")) {
          // 初始化12ban3模式
//          session.sendMessage(new TextMessage("Conventional"));
          sendMessageById(userid, "Conventional");
        } else {
//          session.sendMessage(new TextMessage("GameInit"));
          sendMessageById(userid, "GameInit");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      try {
        loginerSessions.get(userid).close();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      removeSession(userid);
    }
  }

  @Override
  public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {
    String mes = (String) message.getPayload();
    String userid = session.getAttributes().get("userid").toString();
    if (mes.equals("heartbeat") ) {
      try {
        session.sendMessage(new TextMessage("heartbeat"));
      } catch (Exception ignored) {}
    } else {
      log.info("来自登陆器的消息"+mes);
    }
    if (mes.contains("RoomId")){
      conventionalMode.sendRoomId(mes, userid);
    }
    if (mes.contains("mimiId")){
      conventionalMode.setMimiId(mes, userid);
    }
    if (mes.contains("ready")){
      conventionalMode.readyForStart(userid);
    }
    if (mes.contains("start")){
      conventionalMode.gameStart(userid);
    }
    if (mes.contains("Ban")){
      conventionalMode.banElf(mes, userid);
    }
    if (mes.contains("ElfFirst")){
      conventionalMode.pickElfFirst(mes, userid);
    }
    if (mes.contains("ElfRemain")){
      conventionalMode.pickElfRemain(mes, userid);
    }
    if (mes.contains("Winner")){
      conventionalMode.checkWinner(mes, userid);
    }
    if (mes.contains("endGame")){
      conventionalMode.afterFightOverClick(userid);
    }
//    if(mes.contains("endGame")){
//      conventionalMode.endGame(userid);
//    }

    if (mes.contains("JoinMatch")){
      conventionalMode.joinMatch(userid);
    }
    if (mes.contains("QuitMatch")){
      conventionalMode.quitMatch(userid);
    }
  }

  @Override
  public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {

  }

  @Override
  public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
    String userid = session.getAttributes().get("userid").toString();
    try {
      session.close();
    } catch (IOException e) {
      log.info(e.getMessage());
    }
    loginerSessions.remove(userid);
    conventionalMode.quitMatch(userid);
    log.info("用户" + userid + "登录器下线");

    if (userid.startsWith("seeraccount")){
      conventionalMode.offLineWhenMatch(userid);
    }
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }

  /**
   * Send message by id.
   *
   * @param text   the text
   */
  public static void sendMessageByGameId(String text, String Player1, String Player2) {
    if (Player1 == null || Player2 == null) {
      return;
    }
    sendMessageById(Player1, text);
    sendMessageById(Player2, text);
  }

  public static void sendMessageById(String userid,String text) {
    try {
      if (userid != null && loginerSessions.containsKey(userid)) {
        synchronized (loginerSessions.get(userid)) {
          loginerSessions.get(userid).sendMessage(new TextMessage(text));
          log.info("向登陆器" + userid + "发送消息：" + text);
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  // 连接前检查连接会话，若会话存在则关闭会话，会话不存在则返回True
  public static boolean checkLoginerState(String userid) {
    if (userid.startsWith("seeraccount") && loginerSessions.containsKey(userid)) {
      synchronized (loginerSessions.get(userid)) {
        try {
          loginerSessions.get(userid).close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return !loginerSessions.containsKey(userid);
  }

  public static boolean checkSession(String userid) {
    return loginerSessions.containsKey(userid);
  }


  private String checkGameInformation(@NotNull String userid) {
    //判断有无对局信息
    if(Boolean.TRUE.equals(redisTemplate.hasKey("game" + userid))){
      String gameId = (String) redisTemplate.opsForHash().get("game" +  userid, "gameId");
      if(gameId == null) {
        return null;
      }
      // 判断是什么模式
      if (Objects.equals(redisTemplate.opsForHash().get(gameId, "GameMode"), "Conventional")) {
        return "Conventional";
      }
      //判断是否为选手
      if(Objects.equals(redisTemplate.opsForHash().get(gameId, "Player1"), userid)
          || Objects.equals(redisTemplate.opsForHash().get(gameId, "Player2"), userid)){
        return "Player";
      }
    }
    return null;
  }
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
   * 强制移出session
   */
  public static void removeSession(String userid) {
    if (userid.startsWith("seeraccount")) {
      loginerSessions.remove(userid);
    }
  }

  public static void showSessions() {
    Iterator<String> itr = loginerSessions.keys().asIterator();
    List<String> tmp = new ArrayList<>();
    while (itr.hasNext()) {
      tmp.add(itr.next());
    }
    log.info(tmp.toString());
  }

//  public static WebSocketSession getSessionByUserid(String userid) {
//    for (WebSocketSession item : loginer) {
//      if (item.getAttributes().get("userid").equals(userid)) {
//        return item;
//      }
//    }
//    return null;
//  }

  public static void heartbeatAll() {
    Iterator<WebSocketSession> itr = loginerSessions.values().iterator();
    while (itr.hasNext()) {
      try {
        itr.next().sendMessage(new TextMessage("heartbeat"));
      } catch (IOException e) {
        log.info(e.getMessage());
      }
    }
  }
}

