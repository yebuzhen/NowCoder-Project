package com.nowcoder.community.util;

public interface CommunityConstant {

  // Activation status
  int ACTIVATION_SUCCESS = 0;
  int ACTIVATION_FAILURE = 1;
  int ACTIVATION_DUPLICATE = 2;

  // Expired time after login
  int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
  int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

  // Entity types
  int ENTITY_TYPE_POST = 1;
  int ENTITY_TYPE_COMMENT = 2;
  int ENTITY_TYPE_USER = 3;

  // Topic types
  String TOPIC_COMMENT = "comment";
  String TOPIC_LIKE = "like";
  String TOPIC_FOLLOW = "follow";
}
