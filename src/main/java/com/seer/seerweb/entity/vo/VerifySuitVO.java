package com.seer.seerweb.entity.vo;

import lombok.Data;

@Data
public class VerifySuitVO {
    private Boolean matchGame;
    private String groupId;
    private String gameId;
    private Integer suitId;
}
