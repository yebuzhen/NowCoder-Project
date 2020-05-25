package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author barea
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile imageFile, Model model) {

        if (imageFile == null) {

            model.addAttribute("error", "No file is uploaded!");
            return "/site/setting";

        }

        String fileName = imageFile.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        if (StringUtils.isBlank(suffix)) {

            model.addAttribute("error", "Wrong file format!");
            return "/site/setting";

        }

        //Generate a random file name
        fileName = CommunityUtil.generateUUID() + suffix;
        //Store address
        File dest = new File(uploadPath + "/" + fileName);

        try{
            //Store the file
            imageFile.transferTo(dest);
        } catch (IOException e) {

            logger.error("Failed to store the file: " + e.getMessage());
            throw new RuntimeException("Failed to store the file, an error occurred in the server!", e);

        }

        //Update the headerUrl
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";

    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        // Change filename according to the store address
        fileName = uploadPath + "/" + fileName;

        //Get filename extension
        String filenameExtension  = fileName.substring(fileName.lastIndexOf(".") + 1);

        //Response as pic
        response.setContentType("image/" + filenameExtension);

        try(FileInputStream fileInputStream = new FileInputStream(fileName);
            OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
            }

        } catch (IOException e) {
            logger.error("Failed to read the header image: " + e.getMessage());
        }

    }

}