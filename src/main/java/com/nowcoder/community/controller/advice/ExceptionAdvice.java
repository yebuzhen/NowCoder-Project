package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** @author barea */
@ControllerAdvice
public class ExceptionAdvice {

  private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

  @ExceptionHandler({Exception.class})
  public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    logger.error("Server error: " + e.getMessage());

    for (StackTraceElement element : e.getStackTrace()) {
      logger.error(element.toString());
    }

    String xRequestWith = request.getHeader("x-requested-with");

    if ("XMLHttpRequest".equals(xRequestWith)) {

      response.setContentType("application/plain;charset=utf-8");
      PrintWriter writer = response.getWriter();
      writer.write(CommunityUtil.getJSONString(1, "Server error!"));

    } else {
      response.sendRedirect(request.getContextPath() + "/error");
    }
  }
}
