package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/** @author barea */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/resources/**");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    // Authentication
    http.authorizeRequests()
        .antMatchers(
            "/user/setting",
            "/user/upload",
            "/discuss/add",
            "/comment/add/**",
            "/letter/**",
            "/notice/**",
            "/like",
            "/follow",
            "/unfollow")
        .hasAnyAuthority(AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR)
        .antMatchers("/discuss/top", "/discuss/wonderful")
        .hasAnyAuthority(AUTHORITY_MODERATOR)
        .antMatchers("/discuss/delete", "/data/**")
        .hasAnyAuthority(AUTHORITY_ADMIN)
        .anyRequest()
        .permitAll()
        .and()
        .csrf()
        .disable();

    // No authentication
    http.exceptionHandling()
        .authenticationEntryPoint(
            new AuthenticationEntryPoint() {
              // Have not logged in
              @Override
              public void commence(
                  HttpServletRequest httpServletRequest,
                  HttpServletResponse httpServletResponse,
                  AuthenticationException e)
                  throws IOException, ServletException {

                String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(xRequestedWith)) {

                  httpServletResponse.setContentType("application/plain;charset=utf-8");
                  PrintWriter writer = httpServletResponse.getWriter();
                  writer.write(CommunityUtil.getJSONString(403, "You have not logged in yet!"));

                } else {
                  httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
                }
              }
            })
        .accessDeniedHandler(
            new AccessDeniedHandler() {
              // Not enough privilege
              @Override
              public void handle(
                  HttpServletRequest httpServletRequest,
                  HttpServletResponse httpServletResponse,
                  AccessDeniedException e)
                  throws IOException, ServletException {

                String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(xRequestedWith)) {

                  httpServletResponse.setContentType("application/plain;charset=utf-8");
                  PrintWriter writer = httpServletResponse.getWriter();
                  writer.write(CommunityUtil.getJSONString(403, "You have no privilege!"));

                } else {
                  httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
                }
              }
            });

    // Spring Security will intercept /logout request and logout automatically
    // We need to overwrite its logic, then we can logout.
    http.logout().logoutUrl("/securityLogout");
  }
}
