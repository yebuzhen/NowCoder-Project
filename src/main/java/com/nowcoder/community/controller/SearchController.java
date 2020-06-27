package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticsearchService;
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

/**
 * @author barea
 */
@Controller
public class SearchController implements CommunityConstant {

  @Autowired
  private ElasticsearchService elasticsearchService;

  @Autowired
  private UserService userService;

  @Autowired
  private LikeService likeService;

  // /search?keyword=xxx
  @RequestMapping(path = "/search", method = RequestMethod.GET)
  public String search(String keyword, Page page, Model model) {

    // Search posts
    org.springframework.data.domain.Page<DiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword, page.getCurrentPage() - 1, page.getLimitInOnePage());

    // Polymerise data
    List<Map<String, Object>> discussPosts = new ArrayList<>();
    if (searchResult != null) {

      for (DiscussPost discussPost : searchResult) {

        Map<String, Object> map = new HashMap<>(3);
        // post
        map.put("post", discussPost);
        // publisher
        map.put("user", userService.findUserById(discussPost.getUserId()));
        // like count
        map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));

        discussPosts.add(map);

      }

    }

    model.addAttribute("discussPosts", discussPosts);
    model.addAttribute("keyword", keyword);

    // Page info
    page.setPath("/search?keyword=" + keyword);
    page.setRowsTotal(searchResult == null ? 0 : (int) searchResult.getTotalElements());

    return "/site/search";

  }

}










