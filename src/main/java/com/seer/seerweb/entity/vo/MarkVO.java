package com.seer.seerweb.entity.vo;

import lombok.Data;

@Data
public class MarkVO {
    private Integer _markID;
    private Integer _bindMoveID; //宝石绑定的技能id，如果没有宝石则为0
    private Integer _obtainTime;
}
