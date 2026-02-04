package com.chefpro.backendjava.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

  //TODO TAMBIEN PUEDE SOBRAR, YA QUE TENEMOS UN HEALTH EN LOGIN CONTROLLER PARA TEMAS DE DEBUG
    @GetMapping
    public String health() {
        return "ChefPro Backend OK";
    }
}
