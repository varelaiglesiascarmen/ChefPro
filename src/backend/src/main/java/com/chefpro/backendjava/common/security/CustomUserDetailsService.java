package com.chefpro.backendjava.common.security;

import com.chefpro.backendjava.common.object.entity.UserLogin;
import com.chefpro.backendjava.repository.CustomUserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomUserRepository customUserRepository;

    public CustomUserDetailsService(CustomUserRepository customUserRepository) {
        this.customUserRepository = customUserRepository;
    }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserLogin userLogin = customUserRepository.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    String roleName = userLogin.getRole().name();

    return User.builder()
      .username(userLogin.getUsername())
      .password(userLogin.getPassword())
      .authorities("ROLE_" + roleName)  // Cambiado
      .build();
  }
}
