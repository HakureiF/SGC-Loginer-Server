package com.seer.seerweb.service.impl;

import com.seer.seerweb.service.SeerSuitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.HashMap;
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
    String Player1 = "";
    String Player2 = "";
    String suit1;
    String suit2;
    if (gameId != null) {
      Player1 = (String) redisTemplate.opsForHash().get(gameId,"Player1");
      Player2 = (String) redisTemplate.opsForHash().get(gameId,"Player2");
    }
    HashMap<String,String> hashMap = new HashMap<>();
    if (gameId != null) {
      suit1 = String.valueOf(redisTemplate.opsForHash().get("game" + Player1, option + "Suit"));
      suit2 = String.valueOf(redisTemplate.opsForHash().get("game" + Player2, option + "Suit"));

      String hidePick = (String) redisTemplate.opsForHash().get(gameId, "HidePick");
      if (suit1.equals("null") || suit1.isBlank()) {
        suit1 = "";
      } else if (hidePick != null && option.equals("Pick")){
        suit1 = hidePick.charAt(0) != '0' ? Player1.equals(userid) ? suit1 : "1" : suit1;
      }
      if (suit2.equals("null") || suit1.isBlank()) {
        suit2 = "";
      } else if (hidePick != null && option.equals("Pick")){
        suit2 = hidePick.charAt(0) != '0' ? Player2.equals(userid) ? suit2 : "1" : suit2;
      }

      hashMap.put("Player1" + option + "Suit", suit1);
      hashMap.put("Player2" + option + "Suit", suit2);

      return hashMap;
    }
    return null;
  }
}




