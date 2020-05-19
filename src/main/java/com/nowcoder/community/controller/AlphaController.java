package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot.";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {

        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());

        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {

            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);

        }

        System.out.println(request.getParameter("code"));

        response.setContentType("text/html;charset=utf-8");

        try (
                PrintWriter writer = response.getWriter();
                ) {
            writer.write("<h1>NewCoder</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
        @RequestParam(name = "current", required = false, defaultValue = "1") int current,
        @RequestParam(name = "limit", required = false, defaultValue = "10") int limit
    ) {

        System.out.println(current);
        System.out.println(limit);
        return "Some students";

    }

    // /student/123
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {

        System.out.println(id);
        return "A student";

    }

    // POST Request
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age) {

        System.out.println(name);
        System.out.println(age);
        return "success";

    }

    // Response to HTML data
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "Jack");
        modelAndView.addObject("age", "45");
        modelAndView.setViewName("demo/view");
        return modelAndView;

    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {

        model.addAttribute("name", "Imperial College London");
        model.addAttribute("age", "113");
        return "/demo/view";

    }

    // Response to JSON data (not synchronised)
    // Java object -> JSON -> JS object
    @RequestMapping(path = "/employee", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmployee() {

        Map<String, Object> employee = new HashMap<>();
        employee.put("name", "Bob");
        employee.put("age", 20);
        employee.put("salary", 1000);
        return employee;

    }

    @RequestMapping(path = "/employees", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmployees() {

        ArrayList<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> employee = new HashMap<>();
        employee.put("name", "Bob");
        employee.put("age", 20);
        employee.put("salary", 1000);
        list.add(employee);

        employee = new HashMap<>();
        employee.put("name", "Alice");
        employee.put("age", 21);
        employee.put("salary", 2000);
        list.add(employee);

        employee = new HashMap<>();
        employee.put("name", "Charlie");
        employee.put("age", 22);
        employee.put("salary", 3000);
        list.add(employee);

        return list;

    }

    //Cookie Example
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {

        //Create cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());

        //Set effective path
        cookie.setPath("/community/alpha");

        //Set live time
        cookie.setMaxAge(60 * 10);

        //Send cookie
        response.addCookie(cookie);

        return "set cookie";

    }

    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) {

        System.out.println(code);
        return "get cookie";

    }

    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) {

        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set session";

    }

    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {

        System.out.println(session.getAttribute("id"));
        System.out.println("name");
        return "get session";

    }

}
