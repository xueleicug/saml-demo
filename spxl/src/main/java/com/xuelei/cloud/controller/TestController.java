package com.xuelei.cloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    
    @GetMapping({"/", "login"})
    public String index() {
        return "login";
    }
    
    @GetMapping({"user", "/user.html"})
    public String user() {
        return "user";
    }

}
