package com.chefpro.backendjava.service;

import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;

public interface UserService {

  UserLoginDto findByEmail(String email);

  Boolean signInUser(LoginRequestDto singInRequest);
}
