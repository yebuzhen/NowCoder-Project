package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class DiscussPostService {

  private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

  @Autowired private DiscussPostMapper discussPostMapper;

  @Autowired private SensitiveFilter sensitiveFilter;

  @Value("${caffeine.posts.max-size}")
  private int maxSize;

  @Value("${caffeine.posts.expire-seconds}")
  private int expireSeconds;

  public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
    return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
  }

  public int findDiscussPostRows(int userId) {
    return discussPostMapper.selectDiscussPostRows(userId);
  }

  public int addDiscussPost(DiscussPost post) {

    if (post == null) {
      throw new IllegalArgumentException("Discuss post argument cannot be empty!");
    }

    // Escape HTML
    post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
    post.setContent(HtmlUtils.htmlEscape(post.getContent()));

    // Censorship
    post.setTitle(sensitiveFilter.filter(post.getTitle()));
    post.setContent(sensitiveFilter.filter(post.getContent()));

    return discussPostMapper.insertDiscussPost(post);
  }

  public DiscussPost findDiscussPostById(int id) {
    return discussPostMapper.selectDiscussPostById(id);
  }

  public int updateCommentCount(int id, int commentCount) {
    return discussPostMapper.updateCommentCount(id, commentCount);
  }

  public int updateType(int id, int type) {
    return discussPostMapper.updateType(id, type);
  }

  public int updateStatus(int id, int status) {
    return discussPostMapper.updateStatus(id, status);
  }

  public int updateScore(int id, double score) {
    return discussPostMapper.updateScore(id, score);
  }
}
