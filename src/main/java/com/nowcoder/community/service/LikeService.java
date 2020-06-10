package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** @author barea */
@Service
public class LikeService {

  @Autowired private RedisTemplate redisTemplate;

  // Like or unlike
  public void like(int userId, int entityType, int entityId) {

    String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
    boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

    if (isMember) {
      redisTemplate.opsForSet().remove(entityLikeKey, userId);
    } else {
      redisTemplate.opsForSet().add(entityLikeKey, userId);
    }
  }

  // Query the count of likes that one entity gets
  public long findEntityLikeCount(int entityType, int entityId) {

    String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
    return redisTemplate.opsForSet().size(entityLikeKey);
  }

  // If someone has already liked one entity
  public int findEntityLikeStatus(int userId, int entityType, int entityId) {

    String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
    return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
  }
}
