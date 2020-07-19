package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

/** @author barea */
public class PostScoreRefreshJob implements Job, CommunityConstant {

  private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
  // Nowcoder epoch
  private static final Date epoch;

  static {
    try {
      epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
    } catch (ParseException e) {
      throw new RuntimeException("Failed to Initialise of Nowcoder epoch");
    }
  }

  @Autowired private RedisTemplate redisTemplate;
  @Autowired private DiscussPostService discussPostService;
  @Autowired private ElasticsearchService elasticsearchService;
  @Autowired private LikeService likeService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {

    String redisKey = RedisKeyUtil.getPostScoreKey();
    BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

    if (operations.size() == 0) {

      logger.info("[Task Canceled] There is not post score need to re-calculate");
      return;
    }

    logger.info("[Task Start] Now start to refresh scores of posts");
    while (operations.size() > 0) {
      refresh((Integer) operations.pop());
    }
    logger.info("[Task Finish] Scores of posts have re-calculated");
  }

  private void refresh(int postId) {

    DiscussPost post = discussPostService.findDiscussPostById(postId);

    if (post == null) {

      logger.error("Cannot find the post when try to refresh the score: id = " + postId);
      return;
    }

    if (post.getStatus() == 2) {

      logger.error(
          "The queried post is already deleted when try to refresh the score: id = " + postId);
      return;
    }

    // If it is wonderful
    boolean isWonderful = post.getStatus() == 1;
    // The number of comments
    int commentCount = post.getCommentCount();
    // The number of likes
    long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

    // Calculate the weight
    double weight = (isWonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
    // Score = log of weight + date distance
    double score =
        Math.log10(Math.max(weight, 1))
            + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
    // Update the score of the post
    discussPostService.updateScore(postId, score);
    // Sync with elasticsearch
    post.setScore(score);
    elasticsearchService.saveDiscussPost(post);
  }
}
