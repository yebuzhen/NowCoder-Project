package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/** @author barea */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  @Value("${community.path.upload}")
  private String uploadPath;

  @Value("${community.path.domain}")
  private String domain;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Autowired private UserService userService;

  @Autowired private HostHolder hostHolder;

  @Autowired private LikeService likeService;

  @Autowired private FollowService followService;

  @Autowired private DiscussPostService discussPostService;

  @Autowired private CommentService commentService;

  @Value("${qiniu.key.access}")
  private String accessKey;

  @Value("${qiniu.key.secret}")
  private String secretKey;

  @Value("${qiniu.bucket.header.name}")
  private String headerBucketName;

  @Value("${qiniu.bucket.header.url}")
  private String headerBucketUrl;

  @LoginRequired
  @RequestMapping(path = "/setting", method = RequestMethod.GET)
  public String getSettingPage(Model model) {

    // Filename
    String fileName = CommunityUtil.generateUUID();
    // Set response message
    StringMap policy = new StringMap();
    policy.put("returnBody", CommunityUtil.getJSONString(0));
    // Generate upload authentication
    Auth auth = Auth.create(accessKey, secretKey);
    String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

    model.addAttribute("uploadToken", uploadToken);
    model.addAttribute("fileName", fileName);

    return "/site/setting";
  }

  // Update avatar URL
  @RequestMapping(path = "/header/url", method = RequestMethod.POST)
  @ResponseBody
  public String updateHeaderUrl(String fileName) {

    if (StringUtils.isBlank(fileName)) {
      return CommunityUtil.getJSONString(1, "The filename cannot be empty!");
    }

    String url = headerBucketUrl + "/" + fileName;
    userService.updateHeader(hostHolder.getUser().getId(), url);

    return CommunityUtil.getJSONString(0);

  }

  // Deprecated
  @LoginRequired
  @RequestMapping(path = "/upload", method = RequestMethod.POST)
  public String uploadHeader(MultipartFile imageFile, Model model) {

    if (imageFile == null) {

      model.addAttribute("error", "No file is uploaded!");
      return "/site/setting";
    }

    String fileName = imageFile.getOriginalFilename();
    String suffix = fileName.substring(fileName.lastIndexOf("."));

    if (StringUtils.isBlank(suffix)) {

      model.addAttribute("error", "Wrong file format!");
      return "/site/setting";
    }

    // Generate a random file name
    fileName = CommunityUtil.generateUUID() + suffix;
    // Store address
    File dest = new File(uploadPath + "/" + fileName);

    try {
      // Store the file
      imageFile.transferTo(dest);
    } catch (IOException e) {

      logger.error("Failed to store the file: " + e.getMessage());
      throw new RuntimeException("Failed to store the file, an error occurred in the server!", e);
    }

    // Update the headerUrl
    // http://localhost:8080/community/user/header/xxx.png
    User user = hostHolder.getUser();
    String headerUrl = domain + contextPath + "/user/header/" + fileName;
    userService.updateHeader(user.getId(), headerUrl);

    return "redirect:/index";
  }

  // Deprecated
  @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
  public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {

    // Change filename according to the store address
    fileName = uploadPath + "/" + fileName;

    // Get filename extension
    String filenameExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

    // Response as pic
    response.setContentType("image/" + filenameExtension);

    try (FileInputStream fileInputStream = new FileInputStream(fileName);
        OutputStream outputStream = response.getOutputStream()) {

      byte[] buffer = new byte[1024];
      int size = 0;
      while ((size = fileInputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, size);
      }

    } catch (IOException e) {
      logger.error("Failed to read the header image: " + e.getMessage());
    }
  }

  // 修改密码
  @LoginRequired
  @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
  public String updatePassword(String oldPassword, String newPassword, Model model) {

    User user = hostHolder.getUser();
    Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);

    if (map == null || map.isEmpty()) {
      return "redirect:/logout";
    } else {

      model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
      model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
      return "/site/setting";
    }
  }

  // Personal profile page
  @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
  public String getProfilePage(@PathVariable("userId") int userId, Model model) {

    User user = userService.findUserById(userId);
    if (user == null) {
      throw new RuntimeException("No such user!");
    }

    // User
    model.addAttribute("user", user);
    // Number of likes
    int likeCount = likeService.findUserLikeCount(userId);
    model.addAttribute("likeCount", likeCount);

    // Number of entities(only users for now) this user follows
    long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
    model.addAttribute("followeeCount", followeeCount);

    // Number of followers this user has
    long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
    model.addAttribute("followerCount", followerCount);

    // If current user has followed this user
    boolean hasFollowed = false;
    if (hostHolder.getUser() != null) {
      hasFollowed =
          followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
    model.addAttribute("hasFollowed", hasFollowed);

    return "/site/profile";
  }

  // 我的帖子
  @RequestMapping(path = "/mypost/{userId}", method = RequestMethod.GET)
  public String getMyPost(@PathVariable("userId") int userId, Page page, Model model) {
    User user = userService.findUserById(userId);
    if (user == null) {
      throw new RuntimeException("该用户不存在！");
    }
    model.addAttribute("user", user);

    // 分页信息
    page.setPath("/user/mypost/" + userId);
    page.setRowsTotal(discussPostService.findDiscussPostRows(userId));

    // 帖子列表
    List<DiscussPost> discussList =
        discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimitInOnePage(), 0);
    List<Map<String, Object>> discussVOList = new ArrayList<>();
    if (discussList != null) {
      for (DiscussPost post : discussList) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("discussPost", post);
        map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
        discussVOList.add(map);
      }
    }
    model.addAttribute("discussPosts", discussVOList);

    return "/site/my-post";
  }

  // 我的回复
  @RequestMapping(path = "/myreply/{userId}", method = RequestMethod.GET)
  public String getMyReply(@PathVariable("userId") int userId, Page page, Model model) {
    User user = userService.findUserById(userId);
    if (user == null) {
      throw new RuntimeException("该用户不存在！");
    }
    model.addAttribute("user", user);

    // 分页信息
    page.setPath("/user/myreply/" + userId);
    page.setRowsTotal(commentService.findUserCount(userId));

    // 回复列表
    List<Comment> commentList =
        commentService.findUserComments(userId, page.getOffset(), page.getLimitInOnePage());
    List<Map<String, Object>> commentVOList = new ArrayList<>();
    if (commentList != null) {
      for (Comment comment : commentList) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("comment", comment);
        DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
        map.put("discussPost", post);
        commentVOList.add(map);
      }
    }
    model.addAttribute("comments", commentVOList);

    return "/site/my-reply";
  }
}
