package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
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

        //Validate the username
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {

            map.put("usernameMsg", "Username is already registered!");
            return map;

        }

        //Validate the email address
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {

            map.put("emailMsg", "Email is already registered!");
            return map;

        }

        //Register the user
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //Activation mail
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
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
            return ACTIVATION_SUCCESS;

        } else {
            return ACTIVATION_FAILURE;
        }

    }

}
