package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

@Service
public class CommentService implements CommunityConstant {

  @Autowired private CommentMapper commentMapper;

  @Autowired private SensitiveFilter sensitiveFilter;

  @Autowired private DiscussPostService discussPostService;

  public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit) {
    return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
  }

  public int findCommentCount(int entityType, int entityId) {
    return commentMapper.selectCountByEntity(entityType, entityId);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
  public int addComment(Comment comment) {

    if (comment == null) {
      throw new IllegalArgumentException("The comment object cannot be null!");
    }

    // Add the comment
    comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
    comment.setContent(sensitiveFilter.filter(comment.getContent()));
    int rows = commentMapper.insertComment(comment);

    // Update comment count of the post
    if (comment.getEntityType() == ENTITY_TYPE_POST) {

      int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
      discussPostService.updateCommentCount(comment.getEntityId(), count);
    }

    return rows;
  }

  public Comment findCommentById(int id) {
    return commentMapper.selectCommentById(id);
  }

  public List<Comment> findUserComments(int userId, int offset, int limit) {
    return commentMapper.selectCommentsByUser(userId, offset, limit);
  }

  public int findUserCount(int userId) {
    return commentMapper.selectCountByUser(userId);
  }
}
