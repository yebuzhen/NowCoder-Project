package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

  @Autowired private DiscussPostService discussPostService;

  @Autowired private UserService userService;

  @Autowired private HostHolder hostHolder;

  @Autowired private CommentService commentService;

  @Autowired private LikeService likeService;

  @Autowired private EventProducer eventProducer;

  @Autowired private RedisTemplate redisTemplate;

  @RequestMapping(path = "/add", method = RequestMethod.POST)
  @ResponseBody
  public String addDiscussPost(String title, String content) {

    User user = hostHolder.getUser();

    if (user == null) {
      return CommunityUtil.getJSONString(403, "You have not logged in!");
    }

    DiscussPost post = new DiscussPost();
    post.setUserId(user.getId());
    post.setTitle(title);
    post.setContent(content);
    post.setCreateTime(new Date());
    discussPostService.addDiscussPost(post);

    // Trigger the publish event
    Event event =
        new Event()
            .setTopic(TOPIC_PUBLISH)
            .setUserId(user.getId())
            .setEntityType(ENTITY_TYPE_POST)
            .setEntityId(post.getId());
    eventProducer.fireEvent(event);

    // Put the post into Redis to calculate the initial score
    String redisKey = RedisKeyUtil.getPostScoreKey();
    redisTemplate.opsForSet().add(redisKey, post.getId());

    return CommunityUtil.getJSONString(0, "Posted!");
  }

  @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
  public String getDiscussPost(
      @PathVariable("discussPostId") int discussPostId, Model model, Page page) {

    // Post
    DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
    model.addAttribute("post", post);
    // Author user
    User user = userService.findUserById(post.getUserId());
    model.addAttribute("user", user);
    // The count of likes
    long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
    model.addAttribute("likeCount", likeCount);
    // If already liked
    int likeStatus =
        hostHolder.getUser() == null
            ? 0
            : likeService.findEntityLikeStatus(
                hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
    model.addAttribute("likeStatus", likeStatus);

    // Page info of comments
    page.setLimitInOnePage(5);
    page.setPath("/discuss/detail/" + discussPostId);
    page.setRowsTotal(post.getCommentCount());

    // Comment: the comment to the post
    // Reply: the comment to the comment

    // Comment list
    List<Comment> commentList =
        commentService.findCommentByEntity(
            ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimitInOnePage());

    // Comment view object (VO) list
    List<Map<String, Object>> commentVoList = new ArrayList<>();

    if (commentList != null) {

      for (Comment comment : commentList) {

        // Comment VO
        Map<String, Object> commentVo = new HashMap<>();
        // Comment
        commentVo.put("comment", comment);
        // Author of the comment
        commentVo.put("user", userService.findUserById(comment.getUserId()));
        // The count of likes
        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
        commentVo.put("likeCount", likeCount);
        // If already liked
        likeStatus =
            hostHolder.getUser() == null
                ? 0
                : likeService.findEntityLikeStatus(
                    hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
        commentVo.put("likeStatus", likeStatus);

        // Reply list
        List<Comment> replyList =
            commentService.findCommentByEntity(
                ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

        // Reply VO list
        List<Map<String, Object>> replyVoList = new ArrayList<>();

        if (replyList != null) {

          for (Comment reply : replyList) {

            Map<String, Object> replyVo = new HashMap<>();
            // Reply
            replyVo.put("reply", reply);
            // Author of the reply
            replyVo.put("user", userService.findUserById(reply.getUserId()));
            // Reply target
            User target =
                reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
            replyVo.put("target", target);
            // The count of likes
            likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
            replyVo.put("likeCount", likeCount);
            // If already liked
            likeStatus =
                hostHolder.getUser() == null
                    ? 0
                    : likeService.findEntityLikeStatus(
                        hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
            replyVo.put("likeStatus", likeStatus);

            replyVoList.add(replyVo);
          }
        }

        commentVo.put("replies", replyVoList);

        // The number of replies to one comment
        int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
        commentVo.put("replyCount", replyCount);

        commentVoList.add(commentVo);
      }
    }

    model.addAttribute("comments", commentVoList);

    return "/site/discuss-detail";
  }

  // Set to the top
  @RequestMapping(path = "/top", method = RequestMethod.POST)
  @ResponseBody
  public String setTop(int id) {

    discussPostService.updateType(id, 1);

    // Trigger the publish event
    Event event =
        new Event()
            .setTopic(TOPIC_PUBLISH)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(ENTITY_TYPE_POST)
            .setEntityId(id);
    eventProducer.fireEvent(event);

    return CommunityUtil.getJSONString(0);
  }

  // Set as wonderful
  @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
  @ResponseBody
  public String setWonderful(int id) {

    discussPostService.updateStatus(id, 1);

    // Trigger the publish event
    Event event =
        new Event()
            .setTopic(TOPIC_PUBLISH)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(ENTITY_TYPE_POST)
            .setEntityId(id);
    eventProducer.fireEvent(event);

    // Put the post into Redis to calculate the new score
    String redisKey = RedisKeyUtil.getPostScoreKey();
    redisTemplate.opsForSet().add(redisKey, id);

    return CommunityUtil.getJSONString(0);
  }

  // Delete
  @RequestMapping(path = "/delete", method = RequestMethod.POST)
  @ResponseBody
  public String setDelete(int id) {

    discussPostService.updateStatus(id, 2);

    // Trigger the publish event
    Event event =
        new Event()
            .setTopic(TOPIC_DELETE)
            .setUserId(hostHolder.getUser().getId())
            .setEntityType(ENTITY_TYPE_POST)
            .setEntityId(id);
    eventProducer.fireEvent(event);

    return CommunityUtil.getJSONString(0);
  }
}
