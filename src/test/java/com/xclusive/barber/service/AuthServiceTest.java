package com.xclusive.barber.service;

import com.xclusive.barber.dto.auth.AuthResponse;
import com.xclusive.barber.dto.auth.LoginRequest;
import com.xclusive.barber.dto.auth.RegisterClientRequest;
import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.entity.User;
import com.xclusive.barber.enums.Role;
import com.xclusive.barber.exception.DuplicateResourceException;
import com.xclusive.barber.repository.BarberProfileRepository;
import com.xclusive.barber.repository.ClientProfileRepository;
import com.xclusive.barber.repository.UserRepository;
import com.xclusive.barber.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock ClientProfileRepository clientProfileRepository;
    @Mock BarberProfileRepository barberProfileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;

    @InjectMocks AuthService authService;

    private RegisterClientRequest buildRegisterRequest() {
        RegisterClientRequest req = new RegisterClientRequest();
        req.setEmail("nuevo@test.com");
        req.setPassword("Secret123!");
        req.setPhone("3001234567");
        return req;
    }

    @Test
    void testRegisterClient_success() {
        RegisterClientRequest req = buildRegisterRequest();

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(clientProfileRepository.existsByPhone(req.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashed");

        User savedUser = User.builder().id(10L).email(req.getEmail())
                .passwordHash("hashed").role(Role.CLIENT).active(true).build();
        when(userRepository.save(any())).thenReturn(savedUser);
        when(clientProfileRepository.save(any())).thenReturn(mock(ClientProfile.class));
        when(jwtTokenProvider.generateToken(savedUser)).thenReturn("jwt-token");

        AuthResponse response = authService.registerClient(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("CLIENT");
        assertThat(response.getUserId()).isEqualTo(10L);

        verify(userRepository).save(argThat(u -> u.getRole() == Role.CLIENT));
        verify(clientProfileRepository).save(any());
    }

    @Test
    void testRegisterClient_duplicateEmail() {
        RegisterClientRequest req = buildRegisterRequest();
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.registerClient(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_invalidCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("wrongpassword");

        User user = User.builder().id(1L).email(req.getEmail())
                .passwordHash("correct-hash").role(Role.CLIENT).active(true).build();
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), "correct-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtTokenProvider, never()).generateToken(any());
    }
}
