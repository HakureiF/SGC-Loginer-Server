package com.seer.seerweb.service;

import com.seer.seerweb.utils.ResultUtil;

import java.util.HashMap;
import java.util.List;


/**
 * @author Glory.
 *
 * {@code @description} 针对表【GameInformation】的数据库操作Service
 * {@code @createDate} 2023-06-22 22:59:49
 * The interface Game information service.
 */
public interface GameInformationService {
  HashMap<String,String> generateConventionalGame(String groupId,String userid, String conventionalMode);
  List<String> getPickList(String userid,String gameId,String playerId);
  ResultUtil<String> exitGame(String id);
  ResultUtil<String> joinConventionalGame(String id, HashMap<String, String> gameInfo);
  ResultUtil<String> setConventionalSuit(String userid, Integer suitId);
  HashMap<String, String> getDoubleGameHash(String userid, String key);
}
