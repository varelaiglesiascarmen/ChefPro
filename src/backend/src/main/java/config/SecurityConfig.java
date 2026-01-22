package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {

        UserDetails comensal = User.withUsername("comensal")
                .password("$2a$10$.MVmGxV6q1g2mAb3Jqy4TOO9LBq/1O7JS7jO7VTmzr1JlUH0SuYsS")   // hash de la contraseña de comensal
                .roles("COMENSAL")
                .build();

        UserDetails chef = User.withUsername("chef")
                .password("H$2a$10$GmgEkK85jPFt1mitnaElrOhDqsKhfNTBgruPToxMB2Pb9xxXZt4ya")       // hash de la contraseña de chef
                .roles("CHEF")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password("$2a$10$YSKSWl46OPqihGhBGuEyEO8tSDHasdRaUH2fa60ZjSwtxru1Hjjgi")      // hash de la contraseña de admin
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(comensal, chef, admin);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.permitAll())
                .logout(logout -> logout.permitAll());

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

