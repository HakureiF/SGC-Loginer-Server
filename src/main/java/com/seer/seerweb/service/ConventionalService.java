package com.seer.seerweb.service;

import com.seer.seerweb.entity.vo.BagPetVO;
import com.seer.seerweb.entity.vo.VerifyBagVO;
import com.seer.seerweb.entity.vo.VerifySuitVO;

import java.util.List;
import java.util.Map;

public interface ConventionalService {
    String verifyBag(VerifyBagVO bagVO);

    String freshBag(String id, List<BagPetVO> vo);

    Map<String, List<BagPetVO>> getPetState(String id) throws Exception;

    Integer scoreReduced(String groupId, List<BagPetVO> bagPetVOList, boolean isOffline);

    List<Map<String, Object>> getMatchScoreBoard(String groupId);

    String verifySuit(VerifySuitVO vo);

    Integer getBanNum(String id);
}
