package com.seer.seerweb.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;

/**
 *
 * {@code @TableName} SeerElf
 */
@TableName(value ="SeerElf")
public class SeerElf implements Serializable {
    /**
     *
     */
    @TableId
    private Integer id;

    /**
     *
     */
    private String defname;

    /**
     *
     */
    private Integer typeid;

    /**
     *
     */
    private Integer levelstar;

    /**
     *
     */
    private Integer pickednum;

    /**
     *
     */
    private Integer bannednum;

    /**
     *
     */
    private String positionleft;

    /**
     *
     */
    private String positiontop;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     */
    public String getDefname() {
        return defname;
    }

    /**
     *
     */
    public void setDefname(String defname) {
        this.defname = defname;
    }

    /**
     *
     */
    public Integer getTypeid() {
        return typeid;
    }

    /**
     *
     */
    public void setTypeid(Integer typeid) {
        this.typeid = typeid;
    }

    /**
     *
     */
    public Integer getLevelstar() {
        return levelstar;
    }

    /**
     *
     */
    public void setLevelstar(Integer levelstar) {
        this.levelstar = levelstar;
    }

    /**
     *
     */
    public Integer getPickednum() {
        return pickednum;
    }

    /**
     *
     */
    public void setPickednum(Integer pickednum) {
        this.pickednum = pickednum;
    }

    /**
     *
     */
    public Integer getBannednum() {
        return bannednum;
    }

    /**
     *
     */
    public void setBannednum(Integer bannednum) {
        this.bannednum = bannednum;
    }

    /**
     *
     */
    public String getPositionleft() {
        return positionleft;
    }

    /**
     *
     */
    public void setPositionleft(String positionleft) {
        this.positionleft = positionleft;
    }

    /**
     *
     */
    public String getPositiontop() {
        return positiontop;
    }

    /**
     *
     */
    public void setPositiontop(String positiontop) {
        this.positiontop = positiontop;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        SeerElf other = (SeerElf) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getDefname() == null ? other.getDefname() == null : this.getDefname().equals(other.getDefname()))
            && (this.getTypeid() == null ? other.getTypeid() == null : this.getTypeid().equals(other.getTypeid()))
            && (this.getLevelstar() == null ? other.getLevelstar() == null : this.getLevelstar().equals(other.getLevelstar()))
            && (this.getPickednum() == null ? other.getPickednum() == null : this.getPickednum().equals(other.getPickednum()))
            && (this.getBannednum() == null ? other.getBannednum() == null : this.getBannednum().equals(other.getBannednum()))
            && (this.getPositionleft() == null ? other.getPositionleft() == null : this.getPositionleft().equals(other.getPositionleft()))
            && (this.getPositiontop() == null ? other.getPositiontop() == null : this.getPositiontop().equals(other.getPositiontop()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDefname() == null) ? 0 : getDefname().hashCode());
        result = prime * result + ((getTypeid() == null) ? 0 : getTypeid().hashCode());
        result = prime * result + ((getLevelstar() == null) ? 0 : getLevelstar().hashCode());
        result = prime * result + ((getPickednum() == null) ? 0 : getPickednum().hashCode());
        result = prime * result + ((getBannednum() == null) ? 0 : getBannednum().hashCode());
        result = prime * result + ((getPositionleft() == null) ? 0 : getPositionleft().hashCode());
        result = prime * result + ((getPositiontop() == null) ? 0 : getPositiontop().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
            " [" +
            "Hash = " + hashCode() +
            ", id=" + id +
            ", defname=" + defname +
            ", typeid=" + typeid +
            ", levelstar=" + levelstar +
            ", pickednum=" + pickednum +
            ", bannednum=" + bannednum +
            ", positionleft=" + positionleft +
            ", positiontop=" + positiontop +
            ", serialVersionUID=" + serialVersionUID +
            "]";
    }
}
