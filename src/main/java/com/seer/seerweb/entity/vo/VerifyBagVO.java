package com.seer.seerweb.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class VerifyBagVO {
    private Boolean matchGame;
    private String groupId;
    private String gameId;
    private List<BagPetVO> bagInfo;
}
