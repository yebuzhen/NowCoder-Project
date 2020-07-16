package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author barea
 */
@Controller
public class ShareController implements CommunityConstant {

  private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

  @Autowired
  private EventProducer eventProducer;

  @Value("${community.path.domain}")
  private String domain;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  @Value("${wk.image.storage}")
  private String wkImageStorage;

  @RequestMapping(path = "/share", method = RequestMethod.GET)
  @ResponseBody
  public String share(String htmlUrl) {

    // Filename
    String fileName = CommunityUtil.generateUUID();

    // Generate long image asynchronously
    Event event = new Event().setTopic(TOPIC_SHARE)
        .setData("htmlUrl", htmlUrl)
        .setData("fileName", fileName)
        .setData("suffix", ".png");
    eventProducer.fireEvent(event);

    // Return visit address
    Map<String, Object> map = new HashMap<>();
    map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);

    return CommunityUtil.getJSONString(0, null, map);

  }

  // Get the long image
  @RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
  public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {

    if (StringUtils.isBlank(fileName)) {
      throw new IllegalArgumentException("Filename is empty!");
    }

    response.setContentType("image/png");
    File file = new File(wkImageStorage + "/" + fileName + ".png");

    try{

      OutputStream outputStream = response.getOutputStream();
      FileInputStream fileInputStream = new FileInputStream(file);
      byte[] buffer = new byte[1024];
      int bytes;
      while ((bytes = fileInputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytes);
      }

    } catch (IOException e) {
      logger.error("Failed to load the long image: " + e.getMessage());
    }

  }

}










