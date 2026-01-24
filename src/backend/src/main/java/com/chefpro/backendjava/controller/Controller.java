package com.chefpro.backendjava.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class Controller {

    @GetMapping("/whoami")
    public String whoAmI(Principal principal) {
        return "Estás logueado como: " + principal.getName();
    }
}
