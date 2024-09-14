package com.seer.seerweb.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;


import java.io.Serial;
import java.io.Serializable;

/**
 *
 * {@code @TableName} UserInformation
 */
@TableName(value ="UserInformation")
public class UserInformation implements Serializable {
    /**
     * 由数字字母组成的8位id
     */
    @TableId
    @NotBlank(message = "id不能为空")
    @Length(message = "id不多于 {max} 个字符", max = 8)
    @Pattern(regexp = "^[^\\u4e00-\\u9fa5]*$",message = "id不能包含汉字")
    private String userid;

    /**
     * 绑定电话号码
     */
    @NotBlank(message = "电话号码不能为空")
    @Pattern(regexp = "^1[3456789]\\d{9}$",message = "手机号格式错误")
    private String phone;

    /**
     *
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z]).+$",message = "密码格式错误")
    private String password;

    /**
     * 最大十六个字符
     */
    @NotBlank(message = "昵称不能为空")
    @Length(message = "昵称不多于 {max} 个字符", max = 8)
    private String nickname;

    /**
     * 0为普通用户，1为高级用户
     */
    private Integer type;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 由数字字母组成的8位id
     */
    public String getUserid() {
        return userid;
    }

    /**
     * 由数字字母组成的8位id
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * 绑定电话号码
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 绑定电话号码
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     *
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 最大十六个字符
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 最大十六个字符
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 0为普通用户，1为高级用户
     */
    public Integer getType() {
        return type;
    }

    /**
     * 0为普通用户，1为高级用户
     */
    public void setType(Integer type) {
        this.type = type;
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
        UserInformation other = (UserInformation) that;
        return (this.getUserid() == null ? other.getUserid() == null : this.getUserid().equals(other.getUserid()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getPassword() == null ? other.getPassword() == null : this.getPassword().equals(other.getPassword()))
            && (this.getNickname() == null ? other.getNickname() == null : this.getNickname().equals(other.getNickname()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserid() == null) ? 0 : getUserid().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getNickname() == null) ? 0 : getNickname().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
            " [" +
            "Hash = " + hashCode() +
            ", userid=" + userid +
            ", phone=" + phone +
            ", password=" + password +
            ", nickname=" + nickname +
            ", type=" + type +
            ", serialVersionUID=" + serialVersionUID +
            "]";
    }
}
