package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** @author barea */
@Mapper
@Deprecated
public interface LoginTicketMapper {

  @Insert({
    "insert into login_ticket(user_id,ticket,status,expired) ",
    "values(#{userId},#{ticket},#{status},#{expired})"
  })
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insertLoginTicket(LoginTicket loginTicket);

  @Select({"select id,user_id,ticket,status,expired ", "from login_ticket where ticket=#{ticket}"})
  LoginTicket selectByTicket(String ticket);

  // <if> script in @Update is just a demo to show how to use if statement in @Update
  @Update({
    "<script>",
    "update login_ticket set status=#{status} where ticket=#{ticket} ",
    "<if test=\"ticket!=null\"> ",
    "and 1=1 ",
    "</if>",
    "</script>"
  })
  int updateStatus(String ticket, int status);
}
