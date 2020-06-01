package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.HostHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/** @author barea */
@Controller
public class MessageController {

  @Autowired private MessageService messageService;

  @Autowired private HostHolder hostHolder;

  @Autowired private UserService userService;

  // Message list
  @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
  public String getLetterList(Model model, Page page) {

    User user = hostHolder.getUser();

    // page info
    page.setLimitInOnePage(5);
    page.setPath("/letter/list");
    page.setRowsTotal(messageService.findConversationCount(user.getId()));

    // Get latest letter for each conversation on one page
    List<Message> latestLetters =
        messageService.findConversations(user.getId(), page.getOffset(), page.getLimitInOnePage());
    List<Map<String, Object>> conversations = new ArrayList<>();

    if (latestLetters != null) {

      for (Message message : latestLetters) {

        Map<String, Object> map = new HashMap<>();
        map.put("latestLetter", message);
        map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
        map.put(
            "unreadCount",
            messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
        int targetId =
            user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
        map.put("targetUser", userService.findUserById(targetId));

        conversations.add(map);
      }
    }

    model.addAttribute("conversations", conversations);

    // Query the total number of unread messages for the user
    int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
    model.addAttribute("letterUnreadCount", letterUnreadCount);

    return "/site/letter";
  }
}
