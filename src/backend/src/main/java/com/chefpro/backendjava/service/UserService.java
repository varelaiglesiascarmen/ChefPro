package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.SignUpReqDto;
import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.UpdateProfileDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;

public interface UserService {

  UserLoginDto findByEmail(String email);

  public Boolean signUp(SignUpReqDto signUpRequest);

  UserLoginDto updateProfile(String userEmail, UpdateProfileDto updateProfileDto);

}
