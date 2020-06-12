package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.springframework.web.multipart.MultipartFile;

/** @author barea */
@Controller
@RequestMapping("/user")
public class UserController {

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

  @LoginRequired
  @RequestMapping(path = "/setting", method = RequestMethod.GET)
  public String getSettingPage() {
    return "/site/setting";
  }

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

    return "/site/profile";
  }
}
