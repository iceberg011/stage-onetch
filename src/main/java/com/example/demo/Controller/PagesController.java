package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagesController {

    @GetMapping("/Home")
    public String home() {
        return "index";
    }

  
    
    
    // DO NOT add /dashboard here - DashboardController handles it

}