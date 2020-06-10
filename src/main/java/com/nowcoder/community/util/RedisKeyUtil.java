package com.nowcoder.community.util;

/** @author barea */
public class RedisKeyUtil {

  private static final String SPLIT = ":", PREFIX_ENTITY_LIKE = "like:entity";

  // Likes for one entity
  // like:entity:entityType:entityId -> set(userId)
  public static String getEntityLikeKey(int entityType, int entityId) {
    return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
  }
}
