package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
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

  @Value("community.path.domain")
  private String domain;

  @Value("server.servlet.context-path")
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
        .setData("filename", fileName)
        .setData("suffix", ".png");
    eventProducer.fireEvent(event);

    // Return visit address
    Map<String, Object> map = new HashMap<>();
    map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);

    return CommunityUtil.getJSONString(0, null, map);

  }

}
