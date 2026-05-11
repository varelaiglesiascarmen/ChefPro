package com.chefpro.backendjava.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chefpro.backendjava.common.object.dto.SignUpReqDto;
import com.chefpro.backendjava.common.object.dto.login.UpdateProfileDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.common.object.entity.Chef;
import com.chefpro.backendjava.common.object.entity.Diner;
import com.chefpro.backendjava.common.object.entity.UserLogin;
import com.chefpro.backendjava.common.object.entity.UserRoleEnum;
import com.chefpro.backendjava.repository.ChefRepository;
import com.chefpro.backendjava.repository.CustomUserRepository;
import com.chefpro.backendjava.repository.DinerRepository;
import com.chefpro.backendjava.service.UserService;

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
    this.chefRepository = chefRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserLoginDto findByEmail(String email) {
    return customUserRepository.findByUsername(email)
      .map(this::toUserLoginDto)
      .orElse(null);
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

    UserRoleEnum role = UserRoleEnum.DINER;
    if (signUpRequest.getRole() != null && !signUpRequest.getRole().isBlank()) {
      try {
        role = UserRoleEnum.valueOf(signUpRequest.getRole().toUpperCase());
      } catch (IllegalArgumentException ignored) {
        role = UserRoleEnum.DINER;
      }
    }

    UserLogin userLogin = new UserLogin();
    userLogin.setName(signUpRequest.getName() != null ? signUpRequest.getName() : "Usuario");
    userLogin.setLastname(signUpRequest.getSurname() != null ? signUpRequest.getSurname() : "Nuevo");
    userLogin.setUsername(signUpRequest.getUsername());
    userLogin.setEmail(signUpRequest.getEmail());
    userLogin.setPhoneNumber(signUpRequest.getPhoneNumber());
    userLogin.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
    userLogin.setRole(role);

    UserLogin saved = customUserRepository.saveAndFlush(userLogin);

    if (role == UserRoleEnum.CHEF) {
      Chef chef = new Chef();
      chef.setUser(saved);
      chefRepository.save(chef);
    } else {
      Diner diner = new Diner();
      diner.setUser(saved);
      dinerRepository.save(diner);
    }

    return true;
  }

  @Override
  @Transactional
  public UserLoginDto updateProfile(String userEmail, UpdateProfileDto dto) {
    UserLogin userLogin = customUserRepository.findByUsername(userEmail)
      .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    userLogin.setName(dto.getName());
    userLogin.setLastname(dto.getSurname());
    userLogin.setUsername(dto.getUsername());

    if (dto.getPhoto() != null) {
      userLogin.setPhoto(dto.getPhoto().isBlank() ? null : dto.getPhoto());
    }

    if (userLogin.getRole() == UserRoleEnum.CHEF) {
      chefRepository.findByUser(userLogin).ifPresent(chef -> {
        if (dto.getBio() != null)     chef.setBio(dto.getBio());
        if (dto.getPrizes() != null)  chef.setPrizes(dto.getPrizes());
        if (dto.getAddress() != null) chef.setAddress(dto.getAddress());
        chefRepository.save(chef);
      });
    } else if (userLogin.getRole() == UserRoleEnum.DINER) {
      dinerRepository.findByUser(userLogin).ifPresent(diner -> {
        if (dto.getAddress() != null) diner.setAddress(dto.getAddress());
        dinerRepository.save(diner);
      });
    }

    return toUserLoginDto(customUserRepository.save(userLogin));
  }

  @Override
  @Transactional
  public void deleteAccount(String userEmail) {
    UserLogin userLogin = customUserRepository.findByUsername(userEmail)
      .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    if (userLogin.getRole() == UserRoleEnum.CHEF) {
      chefRepository.findByUser(userLogin).ifPresent(chefRepository::delete);
    } else if (userLogin.getRole() == UserRoleEnum.DINER) {
      dinerRepository.findByUser(userLogin).ifPresent(dinerRepository::delete);
    }

    customUserRepository.delete(userLogin);
  }

  @Override
  public boolean existsByUsername(String username) {
    return customUserRepository.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(String email) {
    return customUserRepository.existsByEmail(email);
  }

  private UserLoginDto toUserLoginDto(UserLogin user) {
    UserLoginDto dto = new UserLoginDto();
    dto.setId(user.getId());
    dto.setName(user.getName());
    dto.setSurname(user.getLastname());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setPhoneNumber(user.getPhoneNumber());
    dto.setPhoto(user.getPhoto());
    dto.setRole(user.getRole().name());

    if (user.getRole() == UserRoleEnum.CHEF) {
      chefRepository.findByUser(user).ifPresent(chef -> {
        dto.setBio(chef.getBio());
        dto.setPrizes(chef.getPrizes());
        dto.setAddress(chef.getAddress());
      });
    } else if (user.getRole() == UserRoleEnum.DINER) {
      dinerRepository.findByUser(user).ifPresent(diner -> dto.setAddress(diner.getAddress()));
    }

    return dto;
  }
}
