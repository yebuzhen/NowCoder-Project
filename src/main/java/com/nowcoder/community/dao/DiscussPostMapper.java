package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DiscussPostMapper {

  List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

  // @Param is used to give an alias to the parameter
  // If there is only one parameter, and which would be used in <if>, then the alias is mandatory
  int selectDiscussPostRows(@Param("userId") int userId);

  int insertDiscussPost(DiscussPost discussPost);

  DiscussPost selectDiscussPostById(int id);

  int updateCommentCount(int id, int commentCount);

  int updateType(int id, int type);

  int updateStatus(int id, int status);

  int updateScore(int id, double score);
}
