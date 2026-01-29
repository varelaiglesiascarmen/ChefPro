package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import com.chefpro.backendjava.repository.CustomUserRepository;
import com.chefpro.backendjava.service.UserService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("userService")
public class UserServiceImpl implements UserService {

  private final CustomUserRepository customUserRepository;

  public UserServiceImpl(CustomUserRepository customUserRepository) {
    this.customUserRepository = customUserRepository;
  }

  @Override
  public UserLoginDto findByEmail(String email) {


    Optional<UserLogin> foundUser = customUserRepository.findByUsername(email);

    if(foundUser.isPresent()){

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
}
