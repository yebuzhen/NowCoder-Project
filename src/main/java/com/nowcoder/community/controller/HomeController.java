package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController implements CommunityConstant {

  @Autowired private DiscussPostService discussPostService;

  @Autowired private UserService userService;

  @Autowired private LikeService likeService;

  @RequestMapping(path = "/index", method = RequestMethod.GET)
  public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {

    // Before the method is called, SpringMVC will initialise Model and Page, and put Page into
    // Model.
    // Therefore, thymeleaf can directly use the data in the Page object.
    page.setRowsTotal(discussPostService.findDiscussPostRows(0));
    page.setPath("/index?orderMode=" + orderMode);

    List<DiscussPost> discussPostList =
        discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimitInOnePage(), orderMode);
    List<Map<String, Object>> discussPosts = new ArrayList<>();

    if (discussPostList != null) {

      for (DiscussPost post : discussPostList) {

        Map<String, Object> map = new HashMap<>(3);
        map.put("post", post);
        map.put("user", userService.findUserById(post.getUserId()));
        map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
        discussPosts.add(map);
      }
    }

    model.addAttribute("discussPosts", discussPosts);
    model.addAttribute("orderMode", orderMode);
    return "/index";
  }

  @RequestMapping(path = "/error", method = RequestMethod.GET)
  public String getErrorPage() {
    return "/error/500";
  }

  @RequestMapping(path = "/denied", method = RequestMethod.GET)
  public String getDeniedPage() {
    return "/error/404";
  }
}
