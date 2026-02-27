package com.chefpro.controller;

import com.chefpro.backendjava.common.security.JwtUtil;
import com.chefpro.backendjava.common.object.dto.SignUpReqDto;
import com.chefpro.backendjava.common.object.dto.login.LoginRequestDto;
import com.chefpro.backendjava.common.object.dto.login.LoginResponseDto;
import com.chefpro.backendjava.common.object.dto.login.UpdateProfileDto;
import com.chefpro.backendjava.common.object.dto.login.UserLoginDto;
import com.chefpro.backendjava.controller.LoginController;
import com.chefpro.backendjava.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private UserService userService;

    private LoginController controller;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtUtil               = mock(JwtUtil.class);
        userService           = mock(UserService.class);

        controller = new LoginController(authenticationManager, jwtUtil, userService);
    }

    // Helper para crear un UserDetails con rol mockeado
    private UserDetails mockUserDetails(String username, String role) {
        UserDetails user = mock(UserDetails.class);
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn(role);
        when(user.getAuthorities()).thenReturn((Collection) List.of(authority));
        when(user.getUsername()).thenReturn(username);
        return user;
    }

    // ─── POST /login ─────────────────────────────────────────────────────────

    @Test
    void login_success_returns200WithTokenAndUser() {
        LoginRequestDto req    = mock(LoginRequestDto.class);
        Authentication auth    = mock(Authentication.class);
        UserDetails userDetails = mockUserDetails("chef@example.com", "ROLE_CHEF");
        UserLoginDto userLoginDto = mock(UserLoginDto.class);

        when(req.getUsername()).thenReturn("chef@example.com");
        when(req.getPassword()).thenReturn("secret");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(userService.findByEmail("chef@example.com")).thenReturn(userLoginDto);

        ResponseEntity<LoginResponseDto> response = controller.login(req);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
        verify(userService).findByEmail("chef@example.com");
    }

    @Test
    void login_badCredentials_propagatesException() {
        LoginRequestDto req = mock(LoginRequestDto.class);
        when(req.getUsername()).thenReturn("bad@user.com");
        when(req.getPassword()).thenReturn("wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> controller.login(req));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtil, userService);
    }

    // ─── GET /health ─────────────────────────────────────────────────────────

    @Test
    void health_success_returnsOK() {
        String result = controller.health();

        assertEquals("OK", result);
    }

    @Test
    void health_alwaysReturnsOK_noExternalDependencies() {
        // El endpoint no tiene dependencias; se invoca dos veces y siempre devuelve "OK"
        assertEquals("OK", controller.health());
        assertEquals("OK", controller.health());
        verifyNoInteractions(authenticationManager, jwtUtil, userService);
    }

    // ─── GET /me ─────────────────────────────────────────────────────────────

    @Test
    void me_authenticatedUser_returns200WithUserDto() {
        UserDetails userDetails   = mockUserDetails("chef@example.com", "ROLE_CHEF");
        UserLoginDto userLoginDto  = mock(UserLoginDto.class);
        when(userService.findByEmail("chef@example.com")).thenReturn(userLoginDto);

        ResponseEntity<UserLoginDto> response = controller.me(userDetails);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(userLoginDto, response.getBody());
        verify(userService).findByEmail("chef@example.com");
    }

    @Test
    void me_nullUser_returns401() {
        ResponseEntity<UserLoginDto> response = controller.me(null);

        assertEquals(401, response.getStatusCode().value());
        verifyNoInteractions(userService);
    }

    // ─── POST /signup ────────────────────────────────────────────────────────

    @Test
    void signup_success_returns201WithTokenAndUser() {
        SignUpReqDto req          = mock(SignUpReqDto.class);
        Authentication auth        = mock(Authentication.class);
        UserDetails userDetails    = mockUserDetails("newchef@example.com", "ROLE_CHEF");
        UserLoginDto userLoginDto   = mock(UserLoginDto.class);

        when(req.getUsername()).thenReturn("newchef@example.com");
        when(req.getPassword()).thenReturn("pass123");
        when(userService.signUp(req)).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("new-jwt-token");
        when(userService.findByEmail("newchef@example.com")).thenReturn(userLoginDto);

        ResponseEntity<LoginResponseDto> response = controller.signup(req);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("new-jwt-token", response.getBody().getToken());
        verify(userService).signUp(req);
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void signup_signUpReturnsFalse_returns400() {
        SignUpReqDto req = mock(SignUpReqDto.class);
        when(userService.signUp(req)).thenReturn(false);

        ResponseEntity<LoginResponseDto> response = controller.signup(req);

        assertEquals(400, response.getStatusCode().value());
        verify(userService).signUp(req);
        verifyNoInteractions(authenticationManager, jwtUtil);
    }

    // ─── GET /check-username ─────────────────────────────────────────────────

    @Test
    void checkUsername_notTaken_returnsTrue() {
        when(userService.existsByUsername("available")).thenReturn(false);

        ResponseEntity<Boolean> response = controller.checkUsername("available");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody());
        verify(userService).existsByUsername("available");
    }

    @Test
    void checkUsername_alreadyTaken_returnsFalse() {
        when(userService.existsByUsername("taken")).thenReturn(true);

        ResponseEntity<Boolean> response = controller.checkUsername("taken");

        assertEquals(200, response.getStatusCode().value());
        assertFalse(response.getBody());
        verify(userService).existsByUsername("taken");
    }

    // ─── GET /check-email ────────────────────────────────────────────────────

    @Test
    void checkEmail_notTaken_returnsTrue() {
        when(userService.existsByEmail("free@mail.com")).thenReturn(false);

        ResponseEntity<Boolean> response = controller.checkEmail("free@mail.com");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody());
        verify(userService).existsByEmail("free@mail.com");
    }

    @Test
    void checkEmail_alreadyTaken_returnsFalse() {
        when(userService.existsByEmail("used@mail.com")).thenReturn(true);

        ResponseEntity<Boolean> response = controller.checkEmail("used@mail.com");

        assertEquals(200, response.getStatusCode().value());
        assertFalse(response.getBody());
        verify(userService).existsByEmail("used@mail.com");
    }

    // ─── POST /logout ────────────────────────────────────────────────────────

    @Test
    void logout_success_returns200() {
        ResponseEntity<Void> response = controller.logout();

        assertEquals(200, response.getStatusCode().value());
        verifyNoInteractions(authenticationManager, jwtUtil, userService);
    }

    @Test
    void logout_calledMultipleTimes_alwaysReturns200() {
        assertEquals(200, controller.logout().getStatusCode().value());
        assertEquals(200, controller.logout().getStatusCode().value());
        verifyNoInteractions(authenticationManager, jwtUtil, userService);
    }

    // ─── PUT /profile ────────────────────────────────────────────────────────

    @Test
    void updateProfile_success_returns200WithUpdatedUser() {
        UserDetails userDetails    = mockUserDetails("chef@example.com", "ROLE_CHEF");
        UpdateProfileDto updateDto  = mock(UpdateProfileDto.class);
        UserLoginDto updatedUser    = mock(UserLoginDto.class);
        when(userService.updateProfile("chef@example.com", updateDto)).thenReturn(updatedUser);

        ResponseEntity<UserLoginDto> response = controller.updateProfile(userDetails, updateDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(updatedUser, response.getBody());
        verify(userService).updateProfile("chef@example.com", updateDto);
    }

    @Test
    void updateProfile_nullUser_returns401() {
        UpdateProfileDto updateDto = mock(UpdateProfileDto.class);

        ResponseEntity<UserLoginDto> response = controller.updateProfile(null, updateDto);

        assertEquals(401, response.getStatusCode().value());
        verifyNoInteractions(userService);
    }

    // ─── DELETE /account ─────────────────────────────────────────────────────

    @Test
    void deleteAccount_success_returns200() {
        UserDetails userDetails = mockUserDetails("chef@example.com", "ROLE_CHEF");
        doNothing().when(userService).deleteAccount("chef@example.com");

        ResponseEntity<Void> response = controller.deleteAccount(userDetails);

        assertEquals(200, response.getStatusCode().value());
        verify(userService).deleteAccount("chef@example.com");
    }

    @Test
    void deleteAccount_nullUser_returns401() {
        ResponseEntity<Void> response = controller.deleteAccount(null);

        assertEquals(401, response.getStatusCode().value());
        verifyNoInteractions(userService);
    }
}
