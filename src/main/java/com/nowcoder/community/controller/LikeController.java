package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/** @author barea */
@Controller
public class LikeController implements CommunityConstant {

  @Autowired private LikeService likeService;

  @Autowired private HostHolder hostHolder;

  @Autowired private EventProducer eventProducer;

  @RequestMapping(path = "/like", method = RequestMethod.POST)
  @ResponseBody
  public String like(int entityType, int entityId, int entityUserId, int postId) {

    User user = hostHolder.getUser();

    // Like
    likeService.like(user.getId(), entityType, entityId, entityUserId);

    // Count
    long likeCount = likeService.findEntityLikeCount(entityType, entityId);
    // If already liked
    int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
    // Put results into a map
    Map<String, Object> map = new HashMap<>(2);
    map.put("likeCount", likeCount);
    map.put("likeStatus", likeStatus);

    // Trigger like event
    if (likeStatus == 1) {

      Event event =
          new Event()
              .setTopic(TOPIC_LIKE)
              .setUserId(hostHolder.getUser().getId())
              .setEntityType(entityType)
              .setEntityId(entityId)
              .setEntityUserId(entityUserId)
              .setData("postId", postId);

      eventProducer.fireEvent(event);
    }

    return CommunityUtil.getJSONString(0, null, map);
  }
}
