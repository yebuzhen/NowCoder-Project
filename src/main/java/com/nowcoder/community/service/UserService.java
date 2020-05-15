package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

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

        User u = userMapper.selectByName(user.getUsername());
        if (u == null) {

            map.put("usernameMsg", "Username is already registered!");
            return map;

        }

        return map;

    }

}
