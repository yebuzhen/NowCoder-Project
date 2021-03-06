package com.nowcoder.community.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/** @author barea */
@Component
public class AlphaInterceptor implements HandlerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

  // Before the execution of the controller
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    logger.debug("preHandle: " + handler.toString());
    return true;
  }

  // After the execution of the controller
  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {
    logger.debug("postHandle: " + handler.toString());
  }

  // After the execution of TemplateEngine
  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    logger.debug("afterCompletion: " + handler.toString());
  }
}
