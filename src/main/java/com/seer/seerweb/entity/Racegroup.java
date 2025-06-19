package com.seer.seerweb.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @TableName RaceGroup
 */
@TableName(value ="RaceGroup")
@Data
public class Racegroup implements Serializable {
    /**
     * 比赛组id
     */
    @TableId("GroupId")
    private String groupId;

    /**
     * 比赛组主持人
     */
    @TableField("Host")
    private String host;

    /**
     * 比赛组组名
     */
    @TableField("GroupName")
    private String groupName;

//    @JsonSerialize(using = JsonListSerializer.class)
    @TableField("BanElfList")
    private String banElfList;

    @TableField("PickElfList")
    private String pickElfList;

    @TableField("EnableBan")
    private String enableBan;

    @TableField("EnablePool")
    private String enablePool;

    @TableField(value = "LimitPool")
    private String limitPool;


//    @JsonSerialize(using = JsonListSerializer.class)
//    @JsonDeserialize(using = JsonListDeserialize.class)
    @TableField("BanPickFlow")
    private String banPickFlow;

    @TableField("HidePick")
    private String hidePick; // 从左往右，下标0为套装，下标1~6为精灵。值0表示明手，值1表示普通暗手，值2表示翻牌暗手

    @TableField("ThirdMark")
    private String thirdMark; // 是否允许三刻印精灵

    @TableField("AwardPool")
    private String awardPool; //

    @TableField("LimitMarks")
    private String limitMarks; // 允许的刻印id

    @TableField("MarkStone")
    private String markStone; // 是否允许刻印宝石

    @TableField("DisabledSuits")
    private String disabledSuits; // 禁止的套装

    @TableField("BanNum")
    private Integer banNum;

    @TableField("BanCountTime")
    private Integer banCountTime;

    @TableField("FirstCountTime")
    private Integer firstCountTime;

    @TableField("RemainCountTime")
    private Integer remainCountTime;

    @TableField("PunishPool")
    private String punishPool; //

    @TableField("MaxElfId")
    private Integer maxElfId;

    @TableField("MaxAttackSkillId")
    private Integer maxAttackSkillId;

    @TableField("MaxNormalSkillId")
    private Integer maxNormalSkillId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    @Data
    public class LimitDto {
        private String label;
        private List<Integer> value;
    }
}
