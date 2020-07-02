package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/** @author barea */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

  @Autowired private UserService userService;

  @Autowired private HostHolder hostHolder;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    // Get the login ticket from the cookie
    String ticketStringInCookie = CookieUtil.getValue(request, "ticket");

    if (ticketStringInCookie != null) {

      // Query the stored ticket
      LoginTicket ticketInDataBase = userService.findLoginTicket(ticketStringInCookie);

      // Check if two tickets match
      if (ticketInDataBase != null
          && ticketInDataBase.getStatus() == 0
          && ticketInDataBase.getExpired().after(new Date())) {

        // Query the user
        User user = userService.findUserById(ticketInDataBase.getUserId());

        // Store the user in this request
        hostHolder.setUser(user);

        // Construct the result of user authentication, and store it in SecurityContext, then Spring
        // Security can grant privilege to the user.
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), userService.getAuthorities(user.getId()));
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
      }
    }

    return true;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {

    User user = hostHolder.getUser();
    if (user != null && modelAndView != null) {
      modelAndView.addObject("loginUser", user);
    }
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {

    hostHolder.clear();
    SecurityContextHolder.clearContext();
  }
}
