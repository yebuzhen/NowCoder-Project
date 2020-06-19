package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/** @author barea */
@Controller
public class FollowController implements CommunityConstant {

  @Autowired private FollowService followService;

  @Autowired private HostHolder hostHolder;

  @Autowired private UserService userService;

  @Autowired
  private EventProducer eventProducer;

  @LoginRequired
  @RequestMapping(path = "/follow", method = RequestMethod.POST)
  @ResponseBody
  public String follow(int entityType, int entityId) {

    User user = hostHolder.getUser();

    followService.follow(user.getId(), entityType, entityId);

    // Trigger follow event
    Event event = new Event()
        .setTopic(TOPIC_FOLLOW)
        .setUserId(hostHolder.getUser().getId())
        .setEntityType(entityType)
        .setEntityId(entityId)
        .setEntityUserId(entityId);

    eventProducer.fireEvent(event);

    return CommunityUtil.getJSONString(0, "Followed successfully!");
  }

  @LoginRequired
  @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
  @ResponseBody
  public String unfollow(int entityType, int entityId) {

    User user = hostHolder.getUser();

    followService.unfollow(user.getId(), entityType, entityId);

    return CommunityUtil.getJSONString(0, "Unfollowed successfully!");
  }

  @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
  public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {

    User pageUser = userService.findUserById(userId);
    if (pageUser == null) {
      throw new RuntimeException("The pageUser ID is not valid!");
    }
    model.addAttribute("pageUser", pageUser);

    page.setLimitInOnePage(5);
    page.setPath("/followees/" + userId);
    page.setRowsTotal((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

    List<Map<String, Object>> followeeUserList =
        followService.findUserFollowees(userId, page.getOffset(), page.getLimitInOnePage());

    if (followeeUserList != null) {

      for (Map<String, Object> map : followeeUserList) {

        User followeeUser = (User) map.get("user");
        map.put("hasFollowed", currentUserHasFollowed(followeeUser.getId()));
      }
    }

    model.addAttribute("followeeUsers", followeeUserList);
    return "/site/followee";
  }

  @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
  public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {

    User pageUser = userService.findUserById(userId);
    if (pageUser == null) {
      throw new RuntimeException("The pageUser ID is not valid!");
    }
    model.addAttribute("pageUser", pageUser);

    page.setLimitInOnePage(5);
    page.setPath("/followers/" + userId);
    page.setRowsTotal((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

    List<Map<String, Object>> followerUserList =
        followService.findUserFollowers(userId, page.getOffset(), page.getLimitInOnePage());

    if (followerUserList != null) {

      for (Map<String, Object> map : followerUserList) {

        User followerUser = (User) map.get("user");
        map.put("hasFollowed", currentUserHasFollowed(followerUser.getId()));
      }
    }

    model.addAttribute("followerUsers", followerUserList);
    return "/site/follower";
  }

  // Check if the current user has followed the specified user.
  private boolean currentUserHasFollowed(int userId) {

    if (hostHolder.getUser() == null) {
      return false;
    }

    return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
  }
}
