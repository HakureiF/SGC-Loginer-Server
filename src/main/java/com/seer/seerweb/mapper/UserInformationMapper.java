package com.seer.seerweb.mapper;

import com.seer.seerweb.entity.UserInformation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author Glory
* {@code @description} 针对表【UserInformation】的数据库操作Mapper
* {@code @createDate} 2023-06-23 20:59:48
* {@code @Entity} com.seer.seerweb.entity.UserInformation
 */
@Mapper
public interface UserInformationMapper extends BaseMapper<UserInformation> {
  @Select("select UserId, Phone, Password, NickName from UserInformation where Phone = #{phone}")
  List<UserInformation> mapperExistPhone(@Param("phone") String phone);
  @Select("select UserId, Phone, Password, NickName from UserInformation where UserId = #{userid}")
  List<UserInformation> mapperExistId(@Param("userid") String userid);
  @Select(("select Password from UserInformation where UserId = #{userid}"))
  String selectPasswordById(@Param("userid") String userid);

  @Select("select NickName from UserInformation where UserId = #{userid}")
  String selectNicknameByUserid(@Param("userid") String userid);
  @Select("select type from UserInformation where UserId = #{userid}")
  Integer selectTypeByUserid(@Param("userid") String userid);

  @Update("update UserInformation set Password = #{password} where Phone = #{phone}")
  Integer updatePasswordByPhone(@Param("phone") String phone, @Param("password") String password);
}




