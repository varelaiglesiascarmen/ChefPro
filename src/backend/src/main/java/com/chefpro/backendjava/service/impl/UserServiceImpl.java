package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.SignUpReqDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.common.object.entity.Diner;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import com.chefpro.backendjava.common.object.entity.UserRoleEnum;
import com.chefpro.backendjava.repository.ChefRepository;
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
  private final ChefRepository chefRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(CustomUserRepository customUserRepository,
                         DinerRepository dinerRepository,
                         ChefRepository chefRepository,
                         PasswordEncoder passwordEncoder) {
    this.customUserRepository = customUserRepository;
    this.dinerRepository = dinerRepository;
    this.chefRepository= chefRepository;
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
      userLoginDto.setEmail(userLogin.getEmail());
      userLoginDto.setPhoneNumber(userLogin.getPhoneNumber());
      userLoginDto.setRole(userLogin.getRole().name());

      return userLoginDto;
    }

    return null;
  }

  @Override
  @Transactional
  public Boolean signUp(SignUpReqDto signUpRequest) {

    if (signUpRequest == null
      || signUpRequest.getUsername() == null
      || signUpRequest.getPassword() == null
      || signUpRequest.getEmail() == null) {
      return false;
    }

    if (findByEmail(signUpRequest.getEmail()) != null) {
      return false;
    }

    if (customUserRepository.findByUsername(signUpRequest.getUsername()).isPresent() || customUserRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
      return false;
    }

    UserLogin userLogin = new UserLogin();
    userLogin.setName(signUpRequest.getName());
    userLogin.setLastname(signUpRequest.getSurname());
    userLogin.setUsername(signUpRequest.getUsername());
    userLogin.setEmail(signUpRequest.getEmail());
    userLogin.setPhoneNumber(signUpRequest.getPhoneNumber());
    userLogin.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

    UserRoleEnum role = UserRoleEnum.DINER; // default

    if (signUpRequest.getRole() != null && !signUpRequest.getRole().isBlank()) {
      try {
        role = UserRoleEnum.valueOf(signUpRequest.getRole().toUpperCase());
      } catch (IllegalArgumentException ignored) {
        role = UserRoleEnum.DINER;
      }
    }

    userLogin.setRole(role);

    userLogin.setName(signUpRequest.getName() != null ? signUpRequest.getName() : "Usuario");
    userLogin.setLastname(signUpRequest.getSurname() != null ? signUpRequest.getSurname() : "Nuevo");

    UserLogin savedUser = customUserRepository.saveAndFlush(userLogin);

    if (role == UserRoleEnum.CHEF) {
      Chef chef = new Chef();
      chef.setUser(savedUser);
      chefRepository.save(chef);
    } else {
      Diner diner = new Diner();
      diner.setUser(savedUser);
      dinerRepository.save(diner);
    }

    return true;
  }

}
