package com.chefpro.backendjava.service.impl;

import java.util.Optional;

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
      userLoginDto.setSurname(userLogin.getLastname());
      userLoginDto.setUsername(userLogin.getUsername());
      userLoginDto.setEmail(userLogin.getEmail());
      userLoginDto.setPhoneNumber(userLogin.getPhoneNumber());
      userLoginDto.setPhoto(userLogin.getPhoto());
      userLoginDto.setRole(userLogin.getRole().name());

      // Incluir datos específicos de Chef o Diner
      if (userLogin.getRole() == UserRoleEnum.CHEF) {
        Optional<Chef> chefOpt = chefRepository.findByUser(userLogin);
        if (chefOpt.isPresent()) {
          Chef chef = chefOpt.get();
          userLoginDto.setBio(chef.getBio());
          userLoginDto.setPrizes(chef.getPrizes());
          userLoginDto.setAddress(chef.getAddress());
        }
      } else if (userLogin.getRole() == UserRoleEnum.DINER) {
        Optional<Diner> dinerOpt = dinerRepository.findByUser(userLogin);
        if (dinerOpt.isPresent()) {
          userLoginDto.setAddress(dinerOpt.get().getAddress());
        }
      }

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

  @Override
  @Transactional
  public UserLoginDto updateProfile(String userEmail, UpdateProfileDto updateProfileDto) {
    Optional<UserLogin> foundUser = customUserRepository.findByUsername(userEmail);

    if (foundUser.isEmpty()) {
      throw new RuntimeException("Usuario no encontrado");
    }

    UserLogin userLogin = foundUser.get();

    // Actualizar campos básicos del usuario
    userLogin.setName(updateProfileDto.getName());
    userLogin.setLastname(updateProfileDto.getSurname());
    userLogin.setUsername(updateProfileDto.getUsername());

    // Update photo: null means "don't change", empty/blank means "delete", otherwise set new value
    if (updateProfileDto.getPhoto() != null) {
      if (updateProfileDto.getPhoto().isBlank()) {
        userLogin.setPhoto(null);
      } else {
        userLogin.setPhoto(updateProfileDto.getPhoto());
      }
    }

    // Si es un chef, actualizar información adicional
    if (userLogin.getRole() == UserRoleEnum.CHEF) {
      Optional<Chef> chefOpt = chefRepository.findByUser(userLogin);
      if (chefOpt.isPresent()) {
        Chef chef = chefOpt.get();
        if (updateProfileDto.getBio() != null) {
          chef.setBio(updateProfileDto.getBio());
        }
        if (updateProfileDto.getPrizes() != null) {
          chef.setPrizes(updateProfileDto.getPrizes());
        }
        if (updateProfileDto.getAddress() != null) {
          chef.setAddress(updateProfileDto.getAddress());
        }
        chefRepository.save(chef);
      }
    }

    // Guardar usuario actualizado
    UserLogin savedUser = customUserRepository.save(userLogin);

    // Construir y devolver DTO
    UserLoginDto userLoginDto = new UserLoginDto();
    userLoginDto.setId(savedUser.getId());
    userLoginDto.setName(savedUser.getName());
    userLoginDto.setSurname(savedUser.getLastname());
    userLoginDto.setUsername(savedUser.getUsername());
    userLoginDto.setEmail(savedUser.getEmail());
    userLoginDto.setPhoneNumber(savedUser.getPhoneNumber());
    userLoginDto.setPhoto(savedUser.getPhoto());
    userLoginDto.setRole(savedUser.getRole().name());

    // Incluir datos específicos de Chef o Diner
    if (savedUser.getRole() == UserRoleEnum.CHEF) {
      Optional<Chef> chefOpt = chefRepository.findByUser(savedUser);
      if (chefOpt.isPresent()) {
        Chef chef = chefOpt.get();
        userLoginDto.setBio(chef.getBio());
        userLoginDto.setPrizes(chef.getPrizes());
        userLoginDto.setAddress(chef.getAddress());
      }
    } else if (savedUser.getRole() == UserRoleEnum.DINER) {
      Optional<Diner> dinerOpt = dinerRepository.findByUser(savedUser);
      if (dinerOpt.isPresent()) {
        userLoginDto.setAddress(dinerOpt.get().getAddress());
      }
    }

    return userLoginDto;
  }

  @Override
  public boolean existsByUsername(String username) {
    return customUserRepository.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(String email) {
    return customUserRepository.existsByEmail(email);
  }

}
