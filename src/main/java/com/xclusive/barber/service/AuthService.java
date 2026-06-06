package com.xclusive.barber.service;

import com.xclusive.barber.dto.auth.AuthResponse;
import com.xclusive.barber.dto.auth.LoginRequest;
import com.xclusive.barber.dto.auth.RegisterBarberRequest;
import com.xclusive.barber.dto.auth.RegisterClientRequest;
import com.xclusive.barber.entity.BarberProfile;
import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.entity.User;
import com.xclusive.barber.enums.ClientTier;
import com.xclusive.barber.enums.Role;
import com.xclusive.barber.exception.DuplicateResourceException;
import com.xclusive.barber.repository.BarberProfileRepository;
import com.xclusive.barber.repository.ClientProfileRepository;
import com.xclusive.barber.repository.UserRepository;
import com.xclusive.barber.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final BarberProfileRepository barberProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    @Transactional
    public AuthResponse registerClient(RegisterClientRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("email: already in use");
        }
        if (clientProfileRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("phone: already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .active(true)
                .build();
        user = userRepository.save(user);

        ClientProfile profile = ClientProfile.builder()
                .user(user)
                .phone(request.getPhone())
                .loyaltyPoints(0)
                .tier(ClientTier.NEW)
                .build();
        clientProfileRepository.save(profile);

        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    @Transactional
    public AuthResponse registerBarber(RegisterBarberRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("email: already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.BARBER)
                .active(true)
                .build();
        user = userRepository.save(user);

        BarberProfile profile = BarberProfile.builder()
                .user(user)
                .phone(request.getPhone())
                .specialty(request.getSpecialty())
                .build();
        barberProfileRepository.save(profile);

        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }
}
