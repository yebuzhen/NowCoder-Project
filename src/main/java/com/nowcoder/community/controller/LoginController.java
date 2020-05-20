package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author barea
 */
@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {

        Map<String, Object> map = userService.register(user);

        if (map == null || map.isEmpty()) {

            model.addAttribute("msg", "Registered successful! We have sent you an activation email, please activate.");
            model.addAttribute("target", "/index");
            return "/site/operate-result";

        } else {

            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";

        }

    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {

        int result = userService.activation(userId, code);

        if (result == ACTIVATION_SUCCESS) {

            model.addAttribute("msg", "Successfully Activated!");
            model.addAttribute("target", "/login");

        } else if (result == ACTIVATION_DUPLICATE) {

            model.addAttribute("msg", "Your account is already activated!");
            model.addAttribute("target", "/index");

        } else {

            model.addAttribute("msg", "Your activation code is wrong!");
            model.addAttribute("target", "/index");

        }

        return "/site/operate-result";

    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {

        //Generate kaptcha
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //Put kaptcha into session
        session.setAttribute("kaptcha", text);

        //Give the image to the browser
        response.setContentType("image/png");
        try {

            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);

        } catch (IOException e) {
            logger.error("Failed to generate kaptcha!" + e.getMessage());
        }

    }

}
