package com.seer.seerweb.service.impl;

import com.seer.seerweb.service.SeerSuitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Objects;

@Service
public class SeerSuitServiceImpl implements SeerSuitService{
  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  /**
   * @return
   */
  @Override
  public HashMap<String, String> getSuit(String userid,String option) {
    String gameId = (String) redisTemplate.opsForHash().get("game" +  userid,"gameId");
    HashMap<String,String> hashMap = new HashMap<>();
    if (gameId != null) {
      String player1Id = (String) redisTemplate.opsForHash().get(gameId,"Player1");
      String player2Id = (String) redisTemplate.opsForHash().get(gameId,"Player2");
      String suit1 = String.valueOf(redisTemplate.opsForHash().get("game" + player1Id, option + "Suit"));
      String suit2 = String.valueOf(redisTemplate.opsForHash().get("game" + player2Id, option + "Suit"));
      String phase = (String) redisTemplate.opsForHash().get(gameId, option + "Phase");


      boolean b = Objects.equals(phase, "PlayerBanElf") || Objects.equals(phase, "PlayerPickElfFirst")
              && Objects.equals(phase, "PlayerPickElfRemain") && Objects.equals(phase, "WaitingPeriodResult");
      if (Objects.equals(player1Id, userid)) {
        hashMap.put("Player1" + option + "Suit", suit1);
        if (b) {
          hashMap.put("Player2" + option + "Suit", suit2);
        }
      }
      if (Objects.equals(player2Id, userid)) {
        hashMap.put("Player2" + option + "Suit", suit1);
        if (b) {
          hashMap.put("Player1" + option + "Suit", suit2);
        }
      }
      return hashMap;
    }
    return null;
  }
}




