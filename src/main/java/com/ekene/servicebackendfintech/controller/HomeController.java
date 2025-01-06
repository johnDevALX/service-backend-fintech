package com.ekene.servicebackendfintech.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping(value = {"/"})
    public String home() {
        return "redirect:/swagger-ui.html";
    }

    @GetMapping(value = {"/healthz"})
    public ResponseEntity<Object> liveliness() {
        return new ResponseEntity<>("Okv2", HttpStatus.OK);
    }
}
