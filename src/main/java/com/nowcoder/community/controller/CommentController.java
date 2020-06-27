package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

  @Autowired private CommentService commentService;

  @Autowired private HostHolder hostHolder;

  @Autowired private EventProducer eventProducer;

  @Autowired private DiscussPostService discussPostService;

  @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
  public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {

    comment.setUserId(hostHolder.getUser().getId());
    comment.setStatus(0);
    comment.setCreateTime(new Date());
    commentService.addComment(comment);

    // Trigger comment event
    Event event =
        new Event()
            .setTopic(TOPIC_COMMENT)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(comment.getEntityType())
            .setEntityId(comment.getEntityId())
            .setData("postId", discussPostId);

    if (comment.getEntityType() == ENTITY_TYPE_POST) {
      event.setEntityUserId(
          discussPostService.findDiscussPostById(comment.getEntityId()).getUserId());
    } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
      event.setEntityUserId(commentService.findCommentById(comment.getEntityId()).getUserId());
    }

    eventProducer.fireEvent(event);

    if (comment.getEntityType() == ENTITY_TYPE_POST) {

      // Trigger the publish event
      event =
          new Event()
              .setTopic(TOPIC_PUBLISH)
              .setUserId(comment.getUserId())
              .setEntityType(ENTITY_TYPE_POST)
              .setEntityId(discussPostId);
      eventProducer.fireEvent(event);
    }

    return "redirect:/discuss/detail/" + discussPostId;
  }
}
