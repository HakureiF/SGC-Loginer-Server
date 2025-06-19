package com.seer.seerweb.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class BagPetVO {
    private Integer id;
    private Integer catchTime;
    private Integer level;
    private Integer effectID;
    private List<Integer> marks;
    private List<Skill> skillArray;
    private Skill hideSkill;
    private Integer state; //0——无 1——被ban 2——首发 3——出战
    private List<MarkVO> bindMarks;
    private Integer skinId;
}
