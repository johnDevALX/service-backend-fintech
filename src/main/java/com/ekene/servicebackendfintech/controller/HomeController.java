package com.ekene.servicebackendfintech.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping(value = {"/"})
    public String home() {
        return "redirect:/swagger-ui.html";
    }
}
