package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;

import java.util.Optional;

public interface UserService {

  UserLoginDto findByEmail(String email);

  Boolean singUserIn(LoginRequestDto singInRequest);
}
