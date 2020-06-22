package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.HostHolder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/** @author barea */
@Component
public class MessageInterceptor implements HandlerInterceptor {

  @Autowired private HostHolder hostHolder;

  @Autowired private MessageService messageService;

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {

    User user = hostHolder.getUser();

    if (user != null && modelAndView != null) {

      int unreadLetterCount = messageService.findUnreadLetterCount(user.getId(), null);
      int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
      modelAndView.addObject("allUnreadCount", unreadLetterCount + unreadNoticeCount);
    }
  }
}
