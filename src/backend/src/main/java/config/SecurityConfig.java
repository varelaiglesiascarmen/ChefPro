package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails Reyes = User
                .withUsername("Reyes")
                .password("{noop}1234") // {noop} = sin encriptar, solo para pruebas
                .roles("USER")
                .build();
        UserDetails Carmen = User
                .withUsername("Carmen")
                .password("{noop}1234") // {noop} = sin encriptar, solo para pruebas
                .roles("USER")
                .build();
        UserDetails Admin = User
                .withUsername("Admin")
                .password("{noop}1278") // {noop} = sin encriptar, solo para pruebas
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user);
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
}

