package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/** @author barea */
@Service
public class FollowService implements CommunityConstant {

  @Autowired private RedisTemplate redisTemplate;

  @Autowired private UserService userService;

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

  // Query the number of followers that one entity owns
  public long findFollowerCount(int entityType, int entityId) {

    String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
    return redisTemplate.opsForZSet().zCard(followerKey);
  }

  // Query if the user has followed the entity
  public boolean hasFollowed(int userId, int entityType, int entityId) {

    String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
    return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
  }

  // Query the users that one user follows
  public List<Map<String, Object>> findUserFollowees(int userId, int offset, int limit) {

    String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);

    return getMapList(offset, limit, followeeKey);
  }

  // Query the followers that one user has
  public List<Map<String, Object>> findUserFollowers(int userId, int offset, int limit) {

    String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);

    return getMapList(offset, limit, followerKey);
  }

  //Helper function to fetch the list of maps
  private List<Map<String, Object>> getMapList(int offset, int limit, String key) {

    // The returned set is in order
    Set<Integer> targetIds =
        redisTemplate.opsForZSet().reverseRange(key, offset, offset + limit - 1);

    if (targetIds == null) {
      return null;
    }

    List<Map<String, Object>> list = new ArrayList<>();

    for (Integer targetId : targetIds) {

      Map<String, Object> map = new HashMap<>(2);
      User user = userService.findUserById(targetId);
      map.put("user", user);
      Double score = redisTemplate.opsForZSet().score(key, targetId);
      map.put("followTime", new Date(score.longValue()));
      list.add(map);
    }

    return list;
  }
}
