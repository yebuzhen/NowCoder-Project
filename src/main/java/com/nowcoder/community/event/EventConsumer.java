package com.nowcoder.community.event;

import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author barea
 */
@Component
public class EventConsumer implements CommunityConstant {

  private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

  @Autowired
  private MessageService messageService;

  @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
  public void handle

}
