package com.chefpro.service.impl;

import com.chefpro.backendjava.common.object.dto.SignUpReqDto;
import com.chefpro.backendjava.common.object.dto.login.UpdateProfileDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.common.object.entity.*;
import com.chefpro.backendjava.repository.ChefRepository;
import com.chefpro.backendjava.repository.CustomUserRepository;
import com.chefpro.backendjava.repository.DinerRepository;
import com.chefpro.backendjava.service.UserService;
import com.chefpro.backendjava.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

  private UserService userService;

  private CustomUserRepository customUserRepository;
  private DinerRepository dinerRepository;
  private ChefRepository chefRepository;
  private PasswordEncoder passwordEncoder;

  private UserLogin dinerUserLogin;
  private UserLogin chefUserLogin;

  @BeforeEach
  void setUp() {
    customUserRepository = mock(CustomUserRepository.class);
    dinerRepository      = mock(DinerRepository.class);
    chefRepository       = mock(ChefRepository.class);
    passwordEncoder      = mock(PasswordEncoder.class);

    userService = new UserServiceImpl(
      customUserRepository, dinerRepository, chefRepository, passwordEncoder
    );

    dinerUserLogin = mock(UserLogin.class);
    when(dinerUserLogin.getId()).thenReturn(1L);
    when(dinerUserLogin.getName()).thenReturn("Juan");
    when(dinerUserLogin.getLastname()).thenReturn("García");
    when(dinerUserLogin.getUsername()).thenReturn("juan@example.com");
    when(dinerUserLogin.getEmail()).thenReturn("juan@example.com");
    when(dinerUserLogin.getPhoneNumber()).thenReturn("600000001");
    when(dinerUserLogin.getPhoto()).thenReturn(null);
    when(dinerUserLogin.getRole()).thenReturn(UserRoleEnum.DINER);

    chefUserLogin = mock(UserLogin.class);
    when(chefUserLogin.getId()).thenReturn(2L);
    when(chefUserLogin.getName()).thenReturn("Mario");
    when(chefUserLogin.getLastname()).thenReturn("Rossi");
    when(chefUserLogin.getUsername()).thenReturn("mario@example.com");
    when(chefUserLogin.getEmail()).thenReturn("mario@example.com");
    when(chefUserLogin.getPhoneNumber()).thenReturn("600000002");
    when(chefUserLogin.getPhoto()).thenReturn(null);
    when(chefUserLogin.getRole()).thenReturn(UserRoleEnum.CHEF);
  }

  // ─── findByEmail ─────────────────────────────────────────────────────────

  @Test
  void findByEmail_existingDiner_returnsDto() {
    Diner diner = mock(Diner.class);
    when(diner.getAddress()).thenReturn("Calle Falsa 123");
    when(customUserRepository.findByUsername("juan@example.com")).thenReturn(Optional.of(dinerUserLogin));
    when(dinerRepository.findByUser(dinerUserLogin)).thenReturn(Optional.of(diner));

    UserLoginDto result = userService.findByEmail("juan@example.com");

    assertNotNull(result);
    assertEquals("Juan", result.getName());
    assertEquals("DINER", result.getRole());
    verify(customUserRepository).findByUsername("juan@example.com");
  }

  @Test
  void findByEmail_existingChef_returnsDtoWithBioAndPrizes() {
    Chef chef = mock(Chef.class);
    when(chef.getBio()).thenReturn("Chef bio");
    when(chef.getPrizes()).thenReturn("Premio Michelin");
    when(chef.getAddress()).thenReturn("Madrid");

    when(customUserRepository.findByUsername("mario@example.com")).thenReturn(Optional.of(chefUserLogin));
    when(chefRepository.findByUser(chefUserLogin)).thenReturn(Optional.of(chef));

    UserLoginDto result = userService.findByEmail("mario@example.com");

    assertNotNull(result);
    assertEquals("CHEF", result.getRole());
    assertEquals("Chef bio", result.getBio());
    assertEquals("Premio Michelin", result.getPrizes());
    verify(chefRepository).findByUser(chefUserLogin);
  }

  void findByEmail_nonExistingUser_returnsNull() {
    when(customUserRepository.findByUsername("nobody@example.com")).thenReturn(Optional.empty());

    UserLoginDto result = userService.findByEmail("nobody@example.com");

    assertNull(result);
  }

  // ─── signUp ──────────────────────────────────────────────────────────────

  @Test
  void signUp_newDiner_savesUserAndDiner() {
    SignUpReqDto req = mock(SignUpReqDto.class);
    when(req.getUsername()).thenReturn("nuevo@example.com");
    when(req.getPassword()).thenReturn("pass123");
    when(req.getEmail()).thenReturn("nuevo@example.com");
    when(req.getName()).thenReturn("Nuevo");
    when(req.getSurname()).thenReturn("Usuario");
    when(req.getPhoneNumber()).thenReturn("600000099");
    when(req.getRole()).thenReturn("DINER");

    when(customUserRepository.findByUsername("nuevo@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass");

    UserLogin saved = mock(UserLogin.class);
    when(saved.getId()).thenReturn(10L);
    when(saved.getRole()).thenReturn(UserRoleEnum.DINER);
    when(customUserRepository.saveAndFlush(any(UserLogin.class))).thenReturn(saved);

    Boolean result = userService.signUp(req);

    assertTrue(result);
    verify(customUserRepository).saveAndFlush(any(UserLogin.class));
    verify(dinerRepository).save(any(Diner.class));
    verify(chefRepository, never()).save(any());
  }

  @Test
  void signUp_emailAlreadyExists_returnsFalse() {
    SignUpReqDto req = mock(SignUpReqDto.class);
    when(req.getUsername()).thenReturn("juan@example.com");
    when(req.getPassword()).thenReturn("pass123");
    when(req.getEmail()).thenReturn("juan@example.com");

    // findByEmail internamente llama a findByUsername
    when(customUserRepository.findByUsername("juan@example.com")).thenReturn(Optional.of(dinerUserLogin));

    Boolean result = userService.signUp(req);

    assertFalse(result);
    verify(customUserRepository, never()).saveAndFlush(any());
  }

  @Test
  void signUp_nullRequest_returnsFalse() {
    Boolean result = userService.signUp(null);

    assertFalse(result);
    verifyNoInteractions(customUserRepository, dinerRepository, chefRepository, passwordEncoder);
  }

  // ─── updateProfile ───────────────────────────────────────────────────────

  @Test
  void updateProfile_diner_updatesAndReturnsDto() {
    UpdateProfileDto dto = mock(UpdateProfileDto.class);
    when(dto.getName()).thenReturn("Juan Updated");
    when(dto.getSurname()).thenReturn("García");
    when(dto.getUsername()).thenReturn("juan@example.com");
    when(dto.getPhoto()).thenReturn(null);
    when(dto.getAddress()).thenReturn("Nueva Dirección");

    Diner diner = mock(Diner.class);

    when(customUserRepository.findByUsername("juan@example.com")).thenReturn(Optional.of(dinerUserLogin));
    when(dinerRepository.findByUser(dinerUserLogin)).thenReturn(Optional.of(diner));
    when(customUserRepository.save(dinerUserLogin)).thenReturn(dinerUserLogin);
    when(dinerRepository.findByUser(dinerUserLogin)).thenReturn(Optional.of(diner));
    when(diner.getAddress()).thenReturn("Nueva Dirección");

    UserLoginDto result = userService.updateProfile("juan@example.com", dto);

    assertNotNull(result);
    verify(customUserRepository).save(dinerUserLogin);
    verify(diner).setAddress("Nueva Dirección");
  }

  @Test
  void updateProfile_chef_updatesChefFieldsAndReturnsDto() {
    UpdateProfileDto dto = mock(UpdateProfileDto.class);
    when(dto.getName()).thenReturn("Mario Updated");
    when(dto.getSurname()).thenReturn("Rossi");
    when(dto.getUsername()).thenReturn("mario@example.com");
    when(dto.getPhoto()).thenReturn("data:image/jpeg;base64,newphoto");
    when(dto.getBio()).thenReturn("Nueva bio");
    when(dto.getPrizes()).thenReturn("Premio Nuevo");
    when(dto.getAddress()).thenReturn("Barcelona");

    Chef chef = mock(Chef.class);

    when(customUserRepository.findByUsername("mario@example.com")).thenReturn(Optional.of(chefUserLogin));
    when(chefRepository.findByUser(chefUserLogin)).thenReturn(Optional.of(chef));
    when(customUserRepository.save(chefUserLogin)).thenReturn(chefUserLogin);

    UserLoginDto result = userService.updateProfile("mario@example.com", dto);

    assertNotNull(result);
    verify(chefUserLogin).setPhoto("data:image/jpeg;base64,newphoto");
    verify(chef).setBio("Nueva bio");
    verify(chef).setPrizes("Premio Nuevo");
    verify(chef).setAddress("Barcelona");
    verify(chefRepository).save(chef);
  }

  @Test
  void updateProfile_photoBlank_setsPhotoToNull() {
    UpdateProfileDto dto = mock(UpdateProfileDto.class);
    when(dto.getName()).thenReturn("Juan Updated");
    when(dto.getSurname()).thenReturn("Garcia");
    when(dto.getUsername()).thenReturn("juan@example.com");
    when(dto.getPhoto()).thenReturn("  "); // blank -> debe eliminar la foto
    when(dto.getAddress()).thenReturn(null);

    Diner diner = mock(Diner.class);
    when(customUserRepository.findByUsername("juan@example.com")).thenReturn(Optional.of(dinerUserLogin));
    when(dinerRepository.findByUser(dinerUserLogin)).thenReturn(Optional.of(diner));
    when(customUserRepository.save(dinerUserLogin)).thenReturn(dinerUserLogin);

    userService.updateProfile("juan@example.com", dto);

    verify(dinerUserLogin).setPhoto(null);
    verify(customUserRepository).save(dinerUserLogin);
  }

  @Test
  void updateProfile_userNotFound_throwsRuntimeException() {
    UpdateProfileDto dto = mock(UpdateProfileDto.class);
    when(customUserRepository.findByUsername("nobody@example.com")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class,
      () -> userService.updateProfile("nobody@example.com", dto));
    verify(customUserRepository, never()).save(any());
  }

  // ─── existsByUsername ────────────────────────────────────────────────────

  @Test
  void existsByUsername_existing_returnsTrue() {
    when(customUserRepository.existsByUsername("juan@example.com")).thenReturn(true);

    assertTrue(userService.existsByUsername("juan@example.com"));
    verify(customUserRepository).existsByUsername("juan@example.com");
  }

  @Test
  void existsByUsername_nonExisting_returnsFalse() {
    when(customUserRepository.existsByUsername("ghost@example.com")).thenReturn(false);

    assertFalse(userService.existsByUsername("ghost@example.com"));
    verify(customUserRepository).existsByUsername("ghost@example.com");
  }

  // ─── existsByEmail ───────────────────────────────────────────────────────

  @Test
  void existsByEmail_existing_returnsTrue() {
    when(customUserRepository.existsByEmail("juan@example.com")).thenReturn(true);

    assertTrue(userService.existsByEmail("juan@example.com"));
    verify(customUserRepository).existsByEmail("juan@example.com");
  }

  @Test
  void existsByEmail_nonExisting_returnsFalse() {
    when(customUserRepository.existsByEmail("ghost@example.com")).thenReturn(false);

    assertFalse(userService.existsByEmail("ghost@example.com"));
  }

  // ─── deleteAccount ───────────────────────────────────────────────────────

  @Test
  void deleteAccount_diner_deletesUserAndDiner() {
    Diner diner = mock(Diner.class);
    when(customUserRepository.findByUsername("juan@example.com")).thenReturn(Optional.of(dinerUserLogin));
    when(dinerRepository.findByUser(dinerUserLogin)).thenReturn(Optional.of(diner));

    userService.deleteAccount("juan@example.com");

    verify(dinerRepository).delete(diner);
    verify(customUserRepository).delete(dinerUserLogin);
  }

  @Test
  void deleteAccount_userNotFound_throwsRuntimeException() {
    when(customUserRepository.findByUsername("nobody@example.com")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class,
      () -> userService.deleteAccount("nobody@example.com"));
    verify(customUserRepository, never()).delete(any());
  }
}
