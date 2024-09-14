package com.seer.seerweb.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seer.seerweb.handler.JsonListSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @TableName UserConfig
 */
@TableName(value ="UserConfig")
@Data
public class UserConfig implements Serializable {
    /**
     *
     */
    @TableId
    private String userid;

    @TableField("headUrl")
    private String headUrl;

    @TableField("ElfLike")
    @JsonSerialize(using = JsonListSerializer.class)
    private String elfLike;

}
