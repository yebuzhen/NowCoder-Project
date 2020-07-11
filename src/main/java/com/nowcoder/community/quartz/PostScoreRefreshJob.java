package com.nowcoder.community.quartz;

import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
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

/**
 * @author barea
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

  private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private DiscussPostService discussPostService;

  @Autowired
  private ElasticsearchService elasticsearchService;

  // Nowcoder epoch
  private static final Date epoch;

  static {

    try {
      epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
    } catch (ParseException e) {
      throw new RuntimeException("Failed to Initialise of Nowcoder epoch");
    }

  }

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

  }

}










