package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper {

  List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

  int selectCountByEntity(int entityType, int entityId);

  int insertComment(Comment comment);

  Comment selectCommentById(int id);

  List<Comment> selectCommentsByUser(int userId, int offset, int limit);

  int selectCountByUser(int userId);
}
