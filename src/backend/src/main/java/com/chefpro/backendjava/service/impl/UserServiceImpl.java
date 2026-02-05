package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.common.object.entity.Diner;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import com.chefpro.backendjava.common.object.entity.UserRoleEnum;
import com.chefpro.backendjava.repository.CustomUserRepository;
import com.chefpro.backendjava.repository.DinerRepository;
import com.chefpro.backendjava.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component("userService")
public class UserServiceImpl implements UserService {

  private final CustomUserRepository customUserRepository;
  private final DinerRepository dinerRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(CustomUserRepository customUserRepository,
                         DinerRepository dinerRepository,
                         PasswordEncoder passwordEncoder) {
    this.customUserRepository = customUserRepository;
    this.dinerRepository = dinerRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserLoginDto findByEmail(String email) {

    Optional<UserLogin> foundUser = customUserRepository.findByUsername(email);

    if (foundUser.isPresent()) {

      UserLogin userLogin = foundUser.get();

      UserLoginDto userLoginDto = new UserLoginDto();
      userLoginDto.setId(userLogin.getId());
      userLoginDto.setName(userLogin.getName());
      userLoginDto.setEmail(userLogin.getUsername());
      userLoginDto.setRole(userLogin.getRole().name());

      return userLoginDto;
    }

    return null;
  }

  @Override
  @Transactional
  public Boolean signUp(LoginRequestDto signUpRequest) {

    if (signUpRequest == null || signUpRequest.getUsername() == null || signUpRequest.getPassword() == null) {
      return false;
    }

    if (findByEmail(signUpRequest.getUsername()) != null) {
      return false;
    }

    // Crear UserLogin
    UserLogin userLogin = new UserLogin();
    userLogin.setUsername(signUpRequest.getUsername());
    userLogin.setEmail(signUpRequest.getUsername());
    userLogin.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
    userLogin.setRole(UserRoleEnum.DINER);
    userLogin.setName("Usuario");
    userLogin.setLastname("Nuevo");

    UserLogin savedUser = customUserRepository.saveAndFlush(userLogin);

    // Crear Diner asociado - NO establezcas el ID manualmente
    Diner diner = new Diner();
    diner.setUser(savedUser);  // SOLO establece la relación
    // NO hagas: diner.setId(savedUser.getId());

    dinerRepository.save(diner);

    return true;
  }
}

