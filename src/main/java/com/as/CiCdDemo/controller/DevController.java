package com.as.CiCdDemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevController {

    @GetMapping("/dev")
    public String testingController(){
        return "demo controller for dev";
    }
}
