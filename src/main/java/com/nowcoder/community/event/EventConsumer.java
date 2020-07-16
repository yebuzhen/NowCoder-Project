package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** @author barea */
@Component
public class EventConsumer implements CommunityConstant {

  private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

  @Autowired private MessageService messageService;

  @Autowired private DiscussPostService discussPostService;

  @Autowired private ElasticsearchService elasticsearchService;

  @Value("${wk.image.command}")
  private String wkImageCommand;

  @Value("${wk.image.storage}")
  private String wkImageStorage;

  @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
  public void handleMessage(ConsumerRecord record) {

    if (record == null || record.value() == null) {

      logger.error("The message content is empty!");
      return;
    }

    Event event = JSONObject.parseObject(record.value().toString(), Event.class);
    if (event == null) {

      logger.error("The format of message content is illegal!");
      return;
    }

    // Send system message to user
    Message message = new Message();
    message.setFromId(SYSTEM_USER_ID);
    message.setToId(event.getEntityUserId());
    message.setConversationId(event.getTopic());
    message.setCreateTime(new Date());

    Map<String, Object> content = new HashMap<>();
    content.put("userId", event.getUserId());
    content.put("entityType", event.getEntityType());
    content.put("entityId", event.getEntityId());

    if (!event.getData().isEmpty()) {

      for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
        content.put(entry.getKey(), entry.getValue());
      }
    }

    message.setContent(JSONObject.toJSONString(content));
    messageService.addMessage(message);
  }

  // Consume publish topic
  @KafkaListener(topics = {TOPIC_PUBLISH})
  public void handlePublishMessage(ConsumerRecord record) {

    if (record == null || record.value() == null) {

      logger.error("The message content is empty!");
      return;
    }

    Event event = JSONObject.parseObject(record.value().toString(), Event.class);
    if (event == null) {

      logger.error("The format of message content is illegal!");
      return;
    }

    DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
    elasticsearchService.saveDiscussPost(discussPost);
  }

  // Consume delete topic
  @KafkaListener(topics = {TOPIC_DELETE})
  public void handleDeleteMessage(ConsumerRecord record) {

    if (record == null || record.value() == null) {

      logger.error("The message content is empty!");
      return;
    }

    Event event = JSONObject.parseObject(record.value().toString(), Event.class);
    if (event == null) {

      logger.error("The format of message content is illegal!");
      return;
    }

    elasticsearchService.deleteDiscussPost(event.getEntityId());
  }

  // Consume share topic event
  @KafkaListener(topics = TOPIC_SHARE)
  public void handleShareMessage(ConsumerRecord record) {

    if (record == null || record.value() == null) {

      logger.error("The message content is empty!");
      return;
    }

    Event event = JSONObject.parseObject(record.value().toString(), Event.class);
    if (event == null) {

      logger.error("The format of message content is illegal!");
      return;
    }

    String htmlUrl = (String) event.getData().get("htmlUrl");
    String fileName = (String) event.getData().get("fileName");
    String suffix = (String) event.getData().get("suffix");

    String cmd =
        wkImageCommand
            + " --quality 75 "
            + htmlUrl
            + " "
            + wkImageStorage
            + "/"
            + fileName
            + suffix;

    try {

      Runtime.getRuntime().exec(cmd);
      logger.info("Successfully generated long image");

    } catch (IOException e) {
      logger.info("Failed to generate long image");
    }
  }
}
