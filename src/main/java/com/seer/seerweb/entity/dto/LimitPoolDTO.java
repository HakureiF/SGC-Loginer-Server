package com.seer.seerweb.entity.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LimitPoolDTO {
    private int limitNum;
    private List<Integer> count;

    public LimitPoolDTO(int limitNum) {
        this.limitNum = limitNum;
        this.count = new ArrayList<>();
    }
}
