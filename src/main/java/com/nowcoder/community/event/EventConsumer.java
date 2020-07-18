package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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

  @Value("${qiniu.key.access}")
  private String accessKey;

  @Value("${qiniu.key.secret}")
  private String secretKey;

  @Value("${qiniu.bucket.share.name}")
  private String shareBucketName;

  @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

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

    // Start the timer to monitor the image generation, once it is generated, upload it to qiniu
    // cloud
    UploadTask task = new UploadTask(fileName, suffix);
    Future future = threadPoolTaskScheduler.scheduleAtFixedRate(task, 500);
    task.setFuture(future);
  }

  class UploadTask implements Runnable {

    // file name
    private String fileName;
    // file suffix
    private String suffix;
    // the return value
    private Future future;
    // Start time
    private long startTime;
    // Upload time
    private int uploadTimes;

    public UploadTask(String fileName, String suffix) {
      this.fileName = fileName;
      this.suffix = suffix;
      this.startTime = System.currentTimeMillis();
    }

    public void setFuture(Future future) {
      this.future = future;
    }

    @Override
    public void run() {

      // Failed to generate
      if (System.currentTimeMillis() - startTime > 30000) {

        logger.error("Failed to generate image: " + fileName);
        future.cancel(true);
        return;
      }
      // Failed to upload
      if (uploadTimes >= 3) {

        logger.error("Failed to upload image: " + fileName);
        future.cancel(true);
        return;
      }

      String path = wkImageStorage + "/" + fileName + suffix;
      File file = new File(path);
      if (file.exists()) {

        logger.info(String.format("Start %d time uploading.", ++uploadTimes, fileName));
        // Set response info
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // Generate upload auth
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
        // Appoint one upload zone
        UploadManager manager = new UploadManager(new Configuration(Zone.zone0()));

        try {

          // Start to upload the image
          Response response =
              manager.put(path, fileName, uploadToken, null, "image/" + suffix, false);
          // handle response
          JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
          if (jsonObject == null
              || jsonObject.get("code") == null
              || !"0".equals(jsonObject.get("code").toString())) {
            logger.info(String.format("%d time failed to upload [%s]", uploadTimes, fileName));
          } else {

            logger.info(String.format("%d time succeeded to upload [%s]", uploadTimes, fileName));
            future.cancel(true);
          }

        } catch (QiniuException e) {
          logger.info(String.format("%d time failed to upload [%s]", uploadTimes, fileName));
        }

      } else {
        logger.info("Wait for image to be generated.");
      }
    }
  }
}
