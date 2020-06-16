package com.nowcoder.community.util;

/** @author barea */
public class RedisKeyUtil {

  private static final String SPLIT = ":",
      PREFIX_ENTITY_LIKE = "like:entity",
      PREFIX_USER_LIKE = "like:user",
      PREFIX_FOLLOWER = "follower",
      PREFIX_FOLLOWEE = "followee",
      PREFIX_KAPTCHA = "kaptcha",
      PREFIX_TICKET = "ticket",
      PREFIX_USER = "user";

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

  // Entities that one user follows
  // followee:userId:entityType -> zset(entityId,now)
  public static String getFolloweeKey(int userId, int entityType) {
    return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
  }

  // Users that one entity has
  // follower:entityType:entityId -> zset(userId,now)
  public static String getFollowerKey(int entityType, int entityId) {
    return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
  }

  // Login kaptcha
  public static String getKaptchaKey(String ownerCertificate) {
    return PREFIX_KAPTCHA + SPLIT + ownerCertificate;
  }

  // Login ticket
  public static String getTicketKey(String ticket) {
    return PREFIX_TICKET + SPLIT + ticket;
  }

  // User
  public static String getUserKey(int userId) {
    return PREFIX_USER + SPLIT + userId;
  }
}
