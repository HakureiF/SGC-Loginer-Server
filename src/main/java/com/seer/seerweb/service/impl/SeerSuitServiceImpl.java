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
    hashMap.put("Player1" + option + "Suit", "");
    hashMap.put("Player2" + option + "Suit", "");
    if (gameId != null) {
      String player1Id = (String) redisTemplate.opsForHash().get(gameId,"Player1");
      String player2Id = (String) redisTemplate.opsForHash().get(gameId,"Player2");
      Integer suit1 = (Integer) redisTemplate.opsForHash().get("game" + player1Id, option + "Suit");
      Integer suit2 = (Integer) redisTemplate.opsForHash().get("game" + player2Id, option + "Suit");
      String phase = (String) redisTemplate.opsForHash().get(gameId, "phase");


      boolean b = Objects.equals(phase, "PlayerBanElf") || Objects.equals(phase, "PlayerPickElfFirst")
              || Objects.equals(phase, "PlayerPickElfRemain") || Objects.equals(phase, "WaitingPeriodResult");
      if (Objects.equals(player1Id, userid) && suit1 != null) {
        hashMap.put("Player1" + option + "Suit", String.valueOf(suit1));
        if (b && suit2 != null) {
          hashMap.put("Player2" + option + "Suit", String.valueOf(suit2));
        }
      }
      if (Objects.equals(player2Id, userid) && suit2 != null) {
        hashMap.put("Player2" + option + "Suit", String.valueOf(suit2));
        if (b && suit1 != null) {
          hashMap.put("Player1" + option + "Suit", String.valueOf(suit1));
        }
      }
      return hashMap;
    }
    return null;
  }
}




