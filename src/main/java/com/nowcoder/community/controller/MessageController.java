package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

        Map<String, Object> map = new HashMap<>(4);
        map.put("latestLetter", message);
        map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
        map.put(
            "unreadCount",
            messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
        int counterpartId =
            user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
        map.put("counterpartUser", userService.findUserById(counterpartId));

        conversations.add(map);
      }
    }

    model.addAttribute("conversations", conversations);

    // Query the total number of unread messages for the user
    int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
    model.addAttribute("letterUnreadCount", letterUnreadCount);

    return "/site/letter";
  }

  @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
  public String getLetterDetail(
      @PathVariable("conversationId") String conversationId, Page page, Model model) {

    // page info
    page.setLimitInOnePage(5);
    page.setPath("/letter/detail/" + conversationId);
    page.setRowsTotal(messageService.findLetterCount(conversationId));

    // The list of letters of one conversation
    List<Message> letterList =
        messageService.findLetters(conversationId, page.getOffset(), page.getLimitInOnePage());
    List<Map<String, Object>> letters = new ArrayList<>();

    if (letterList != null) {

      for (Message message : letterList) {

        Map<String, Object> map = new HashMap<>();
        map.put("letter", message);
        map.put("fromUser", userService.findUserById(message.getFromId()));
        letters.add(map);
      }
    }

    model.addAttribute("letters", letters);

    // The counterpart of the letter
    model.addAttribute("counterpartUser", getCounterpartUser(conversationId));

    // Set letters as read
    List<Integer> ids = getLetterIds(letterList);
    if (!ids.isEmpty()) {
      messageService.readMessage(ids);
    }

    return "/site/letter-detail";

  }

  private User getCounterpartUser(String conversationId) {

    String[] ids = conversationId.split("_");
    int id0 = Integer.parseInt(ids[0]);
    int id1 = Integer.parseInt(ids[1]);

    return hostHolder.getUser().getId() == id0
        ? userService.findUserById(id1)
        : userService.findUserById(id0);
  }

  private List<Integer> getLetterIds(List<Message> letterList) {

    List<Integer> ids = new ArrayList<>();

    if (letterList != null) {

      for (Message message : letterList) {

        if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
          ids.add(message.getId());
        }

      }

    }

    return ids;

  }

  @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
  @ResponseBody
  public String sendLetter(String toName, String content) {

    User targetUser = userService.findUserByName(toName);
    if (targetUser == null) {
      return CommunityUtil.getJSONString(1, "No such a user!");
    }

    Message message = new Message();
    message.setFromId(hostHolder.getUser().getId());
    message.setToId(targetUser.getId());

    if (message.getFromId() < message.getToId()) {
      message.setConversationId(message.getFromId() + "_" + message.getToId());
    } else {
      message.setConversationId(message.getToId() + "_" + message.getFromId());
    }

    message.setContent(content);
    message.setCreateTime(new Date());
    messageService.addMessage(message);

    return CommunityUtil.getJSONString(0);
  }
}
