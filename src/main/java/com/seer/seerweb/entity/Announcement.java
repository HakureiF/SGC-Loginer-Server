package com.seer.seerweb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * The type Announcement.
 *
 * @TableName Announcement
 */
@TableName(value = "Announcement")
@Data
public class Announcement implements Serializable {
  /**
   * 公告主键.
   */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /**
   * 公告内容.
   */
  private String content;

  /**
   * 发布日期.
   */
  private Date createDate;

  /**
   * 截止日期.
   */
  private Date deadlineDate;

  /**
   * 重要等级.
   */
  private String level;

  /**
   * 公告展示图片.
   */
  private String imgUrl;

  @Serial
  @TableField(exist = false)
  private static final long serialVersionUID = 1L;

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
    Announcement other = (Announcement) that;
    return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
      && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
      && (this.getCreateDate() == null ? other.getCreateDate() == null : this.getCreateDate().equals(other.getCreateDate()))
      && (this.getDeadlineDate() == null ? other.getDeadlineDate() == null : this.getDeadlineDate().equals(other.getDeadlineDate()))
      && (this.getLevel() == null ? other.getLevel() == null : this.getLevel().equals(other.getLevel()))
      && (this.getImgUrl() == null ? other.getImgUrl() == null : this.getImgUrl().equals(other.getImgUrl()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
    result = prime * result + ((getCreateDate() == null) ? 0 : getCreateDate().hashCode());
    result = prime * result + ((getDeadlineDate() == null) ? 0 : getDeadlineDate().hashCode());
    result = prime * result + ((getLevel() == null) ? 0 : getLevel().hashCode());
    result = prime * result + ((getImgUrl() == null) ? 0 : getImgUrl().hashCode());
    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + " ["
        + "Hash = " + hashCode()
        + ", id=" + id
        + ", content=" + content
        + ", createDate=" + createDate
        + ", deadlineDate=" + deadlineDate
        + ", level=" + level
        + ", imgUrl=" + imgUrl
        + ", serialVersionUID=" + serialVersionUID + "]";
  }
}
