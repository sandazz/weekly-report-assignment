package com.weeklyreport.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Demo endpoints proving role-based authorization works; real business controllers come later.
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/team-member")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER','MANAGER','ADMIN')")
    public String teamMember() {
        return "Hello, team member";
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String manager() {
        return "Hello, manager";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "Hello, admin";
    }
}
