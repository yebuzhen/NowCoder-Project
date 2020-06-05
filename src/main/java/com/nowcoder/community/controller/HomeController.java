package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {

  @Autowired private DiscussPostService discussPostService;

  @Autowired private UserService userService;

  @RequestMapping(path = "/index", method = RequestMethod.GET)
  public String getIndexPage(Model model, Page page) {

    // Before the method is called, SpringMVC will initialise Model and Page, and put Page into
    // Model.
    // Therefore, thymeleaf can directly use the data in the Page object.
    page.setRowsTotal(discussPostService.findDiscussPostRows(0));
    page.setPath("/index");

    List<DiscussPost> discussPostList =
        discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimitInOnePage());
    List<Map<String, Object>> discussPosts = new ArrayList<>();

    if (discussPostList != null) {

      for (DiscussPost post : discussPostList) {

        Map<String, Object> map = new HashMap<>();
        map.put("post", post);
        map.put("user", userService.findUserById(post.getUserId()));
        discussPosts.add(map);
      }
    }

    model.addAttribute("discussPosts", discussPosts);
    return "/index";
  }

  @RequestMapping(path = "/error", method = RequestMethod.GET)
  public String getErrorPage() {
    return "/error/500";
  }
}
