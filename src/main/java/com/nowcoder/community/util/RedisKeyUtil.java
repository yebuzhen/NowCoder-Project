package com.nowcoder.community.util;

/** @author barea */
public class RedisKeyUtil {

  private static final String SPLIT = ":",
      PREFIX_ENTITY_LIKE = "like:entity",
      PREFIX_USER_LIKE = "like:user";

  // Likes for one entity
  // like:entity:entityType:entityId -> set(userId)
  public static String getEntityLikeKey(int entityType, int entityId) {
    return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
  }

  // Likes for one user
  // like:user:userId -> int
  public static String getUserLikeKey(int userId) {
    return PREFIX_USER_LIKE + SPLIT + userId;
  }
}
