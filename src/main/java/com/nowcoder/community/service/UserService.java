package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class UserService implements CommunityConstant {

  @Autowired private UserMapper userMapper;

  @Autowired private MailClient mailClient;

  @Autowired private TemplateEngine templateEngine;

  //  @Autowired private LoginTicketMapper loginTicketMapper;

  @Autowired private RedisTemplate redisTemplate;

  @Value("${community.path.domain}")
  private String domain;

  @Value("${server.servlet.context-path}")
  private String contextPath;

  public User findUserById(int id) {

    //    return userMapper.selectById(id);
    User user = getCache(id);

    if (user == null) {
      user = initCache(id);
    }

    return user;
  }

  public Map<String, Object> register(User user) {

    Map<String, Object> map = new HashMap<>();

    if (user == null) {
      throw new IllegalArgumentException("argument cannot be null!");
    }

    if (StringUtils.isBlank(user.getUsername())) {

      map.put("usernameMsg", "Username cannot be empty!");
      return map;
    }

    if (StringUtils.isBlank(user.getPassword())) {

      map.put("passwordMsg", "Password cannot be empty!");
      return map;
    }

    if (StringUtils.isBlank(user.getEmail())) {

      map.put("emailMsg", "Email cannot be empty!");
      return map;
    }

    // Validate the username
    User u = userMapper.selectByName(user.getUsername());
    if (u != null) {

      map.put("usernameMsg", "Username is already registered!");
      return map;
    }

    // Validate the email address
    u = userMapper.selectByEmail(user.getEmail());
    if (u != null) {

      map.put("emailMsg", "Email is already registered!");
      return map;
    }

    // Register the user
    user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
    user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
    user.setType(0);
    user.setStatus(0);
    user.setActivationCode(CommunityUtil.generateUUID());
    user.setHeaderUrl(
        String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
    user.setCreateTime(new Date());
    userMapper.insertUser(user);

    // Activation mail
    Context context = new Context();
    context.setVariable("email", user.getEmail());
    String url =
        domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
    context.setVariable("url", url);
    String content = templateEngine.process("/mail/activation", context);
    mailClient.sendMail(user.getEmail(), "Activation Mail", content);

    return map;
  }

  public int activation(int userId, String code) {

    User user = userMapper.selectById(userId);

    if (user.getStatus() == 1) {
      return ACTIVATION_DUPLICATE;
    } else if (user.getActivationCode().equals(code)) {

      userMapper.updateStatus(userId, 1);
      clearCache(userId);
      return ACTIVATION_SUCCESS;

    } else {
      return ACTIVATION_FAILURE;
    }
  }

  public Map<String, Object> login(String username, String password, long expiredSeconds) {

    Map<String, Object> map = new HashMap<>();

    // Handle empty values
    if (StringUtils.isBlank(username)) {

      map.put("usernameMsg", "Username cannot be empty!");
      return map;
    }
    if (StringUtils.isBlank(password)) {

      map.put("passwordMsg", "Password cannot be empty!");
      return map;
    }

    // Validate Account
    User user = userMapper.selectByName(username);
    if (user == null) {

      map.put("usernameMsg", "Username does not exist!");
      return map;
    }

    // Validate Status
    if (user.getStatus() == 0) {

      map.put("usernameMsg", "Account not activated!");
      return map;
    }

    // Validate Password
    password = CommunityUtil.md5(password + user.getSalt());
    if (!user.getPassword().equals(password)) {

      map.put("passwordMsg", "Password is wrong!");
      return map;
    }

    // Generate login ticket
    LoginTicket loginTicket = new LoginTicket();
    loginTicket.setUserId(user.getId());
    loginTicket.setTicket(CommunityUtil.generateUUID());
    loginTicket.setStatus(0);
    loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * expiredSeconds));
    //    loginTicketMapper.insertLoginTicket(loginTicket);

    String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
    redisTemplate.opsForValue().set(redisKey, loginTicket);

    map.put("ticket", loginTicket.getTicket());
    return map;
  }

  public void logout(String ticket) {

    //    loginTicketMapper.updateStatus(ticket, 1);
    String redisKey = RedisKeyUtil.getTicketKey(ticket);
    LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    loginTicket.setStatus(1);
    redisTemplate.opsForValue().set(redisKey, loginTicket);
  }

  public LoginTicket findLoginTicket(String ticket) {

    //    return loginTicketMapper.selectByTicket(ticket);
    String redisKey = RedisKeyUtil.getTicketKey(ticket);
    return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
  }

  public int updateHeader(int userId, String headerUrl) {

    //    return userMapper.updateHeader(userId, headerUrl);
    int rows = userMapper.updateHeader(userId, headerUrl);
    clearCache(userId);
    return rows;
  }

  public User findUserByName(String username) {
    return userMapper.selectByName(username);
  }

  // 重置密码
  public Map<String, Object> resetPassword(String email, String password) {

    Map<String, Object> map = new HashMap<>();

    // 空值处理
    if (StringUtils.isBlank(email)) {

      map.put("emailMsg", "邮箱不能为空!");
      return map;
    }
    if (StringUtils.isBlank(password)) {

      map.put("passwordMsg", "密码不能为空!");
      return map;
    }

    // 验证邮箱
    User user = userMapper.selectByEmail(email);
    if (user == null) {

      map.put("emailMsg", "该邮箱尚未注册!");
      return map;
    }

    // 重置密码
    password = CommunityUtil.md5(password + user.getSalt());
    userMapper.updatePassword(user.getId(), password);

    map.put("user", user);
    return map;
  }

  // 修改密码
  public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {

    Map<String, Object> map = new HashMap<>();

    // 空值处理
    if (StringUtils.isBlank(oldPassword)) {

      map.put("oldPasswordMsg", "原密码不能为空!");
      return map;
    }
    if (StringUtils.isBlank(newPassword)) {

      map.put("newPasswordMsg", "新密码不能为空!");
      return map;
    }

    // 验证原始密码
    User user = userMapper.selectById(userId);
    oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());

    if (!user.getPassword().equals(oldPassword)) {

      map.put("oldPasswordMsg", "原密码输入有误!");
      return map;
    }

    // 更新密码
    newPassword = CommunityUtil.md5(newPassword + user.getSalt());
    userMapper.updatePassword(userId, newPassword);

    return map;
  }

  // 1. First, we try to get cache from Redis
  private User getCache(int userId) {

    String redisKey = RedisKeyUtil.getUserKey(userId);
    return (User) redisTemplate.opsForValue().get(redisKey);
  }

  // 2. If there is no cache, get data from MySQL and initialise the cache.
  private User initCache(int userId) {

    User user = userMapper.selectById(userId);
    String redisKey = RedisKeyUtil.getUserKey(userId);
    redisTemplate.opsForValue().set(redisKey, user, 1, TimeUnit.HOURS);
    return user;
  }

  // 3. If we change the user data, the cache is deleted.
  private void clearCache(int userId) {

    String redisKey = RedisKeyUtil.getUserKey(userId);
    redisTemplate.delete(redisKey);
  }

  public Collection<? extends GrantedAuthority> getAuthorities(int userId) {

    User user = this.findUserById(userId);

    List<GrantedAuthority> list = new ArrayList<>();
    list.add(
        new GrantedAuthority() {

          @Override
          public String getAuthority() {

            switch (user.getType()) {
              case 1:
                return AUTHORITY_ADMIN;
              case 2:
                return AUTHORITY_MODERATOR;
              default:
                return AUTHORITY_USER;
            }
          }
        });

    return list;
  }
}
