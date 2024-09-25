package com.as.CiCdDemo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DemoController {

    @GetMapping("/test")
    public ResponseEntity<String> demoResponse(){
        return new ResponseEntity<>("hello from development branch !!", HttpStatus.ACCEPTED);
    }
}
