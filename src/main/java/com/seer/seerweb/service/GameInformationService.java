package com.seer.seerweb.service;

import com.seer.seerweb.utils.ResultUtil;

import java.util.HashMap;
import java.util.List;



public interface GameInformationService {
  HashMap<String,String> generateConventionalGame(String groupId,String userid, String conventionalMode, boolean isMatch);
  List<String> getPickList(String userid,String gameId,String playerId);
  ResultUtil<String> exitGame(String id);
  ResultUtil<String> joinConventionalGame(String id, HashMap<String, String> gameInfo);
  ResultUtil<String> setConventionalSuit(String userid, Integer suitId);
  HashMap<String, String> getDoubleGameHash(String userid, String key);
}
