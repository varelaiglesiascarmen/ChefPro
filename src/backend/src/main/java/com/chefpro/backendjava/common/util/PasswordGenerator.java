package com.chefpro.backendjava.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        String adminRaw = "admin1234";
        String chefRaw = "chef1234";
        String comensalRaw = "comensal1234";

        System.out.println("ADMIN    -> " + encoder.encode(adminRaw));
        System.out.println("CHEF     -> " + encoder.encode(chefRaw));
        System.out.println("COMENSAL -> " + encoder.encode(comensalRaw));
    }
}
