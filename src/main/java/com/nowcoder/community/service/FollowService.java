package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/** @author barea */
@Service
public class FollowService {

  @Autowired private RedisTemplate redisTemplate;

  public void follow(int userId, int entityType, int entityId) {

    redisTemplate.execute(
        new SessionCallback() {

          @Override
          public Object execute(RedisOperations operations) throws DataAccessException {

            String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
            String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

            operations.multi();

            operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
            operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

            return operations.exec();
          }
        });
  }

  public void unfollow(int userId, int entityType, int entityId) {

    redisTemplate.execute(
        new SessionCallback() {

          @Override
          public Object execute(RedisOperations operations) throws DataAccessException {

            String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
            String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

            operations.multi();

            operations.opsForZSet().remove(followeeKey, entityId, System.currentTimeMillis());
            operations.opsForZSet().remove(followerKey, userId, System.currentTimeMillis());

            return operations.exec();
          }
        });
  }

  // Query the number of entities that one user follows
  public long findFolloweeCount(int userId, int entityType) {

    String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
    return redisTemplate.opsForZSet().zCard(followeeKey);
  }
}