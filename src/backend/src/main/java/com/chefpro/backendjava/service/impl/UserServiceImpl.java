package com.chefpro.backendjava.service.impl;

import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import com.chefpro.backendjava.common.object.entity.UserRoleEnum;
import com.chefpro.backendjava.repository.CustomUserRepository;
import com.chefpro.backendjava.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component("userService")
public class UserServiceImpl implements UserService {

  private final CustomUserRepository customUserRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(CustomUserRepository customUserRepository, PasswordEncoder passwordEncoder) {
    this.customUserRepository = customUserRepository;
    this.passwordEncoder = passwordEncoder;
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

  @Override
  @Transactional
  public Boolean signInUser(LoginRequestDto request) {

    if(request != null && request.getUsername() != null && request.getPassword() != null){

      if(findByEmail(request.getUsername()) == null) {

        UserLogin userLogin = new UserLogin();
        userLogin.setUsername(request.getUsername());
        userLogin.setPassword(passwordEncoder.encode(request.getPassword()));
        userLogin.setRole(UserRoleEnum.COMENSAL);

        customUserRepository.saveAndFlush(userLogin);

        return true;
      }
    }

    return false;
  }


}
