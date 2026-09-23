package com.nttho.notifycationservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotifycationController {

    public static final List<String> emails = new ArrayList<>();

    @GetMapping("/history")
    public List<String> orderNotifycationHistory(){
        return emails;
    }
}
