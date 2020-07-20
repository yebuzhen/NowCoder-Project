package com.nowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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

  // Caffeine core interface: Cache, LoadingCache, AsyncLoadingCache

  // Cache for post list
  private LoadingCache<String, List<DiscussPost>> postListCache;

  // Cache for post total number
  private LoadingCache<Integer, Integer> postRowsCache;

  @PostConstruct
  public void init() {

    // Init Cache for post list
    postListCache =
        Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
            .build(
                new CacheLoader<>() {

                  @Nullable
                  @Override
                  public List<DiscussPost> load(@NonNull String key) throws Exception {

                    if (key.length() == 0) {
                      throw new IllegalArgumentException("input argument error!");
                    }

                    String[] params = key.split(":");
                    if (params.length != 2) {
                      throw new IllegalArgumentException("input argument error!");
                    }

                    int offset = Integer.parseInt(params[0]);
                    int limit = Integer.parseInt(params[1]);

                    // You can also use second layer cache: Redis -> MySQL

                    logger.debug("Load post list from MySQL");
                    return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                  }
                });

    // Init Cache for post total number
    postRowsCache =
        Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
            .build(
                new CacheLoader<>() {

                  @Nullable
                  @Override
                  public Integer load(@NonNull Integer key) throws Exception {

                    logger.debug("Load post list from MySQL");
                    return discussPostMapper.selectDiscussPostRows(key);
                  }
                });
  }

  public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {

    if (userId == 0 && orderMode == 1) {
      return postListCache.get((offset + ":" + limit));
    }

    logger.debug("Load post list from MySQL");
    return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
  }

  public int findDiscussPostRows(int userId) {

    if (userId == 0) {
      return postRowsCache.get(0);
    }

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
