package org.example.services;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.example.configuration.security.JwtService;
import org.example.dto.account.AuthResponseDto;
import org.example.dto.account.LoginDto;
import org.example.dto.account.RegistrationDto;
import org.example.entities.UserEntity;
import org.example.respositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AccountService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponseDto login(LoginDto request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var isValid = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!isValid) {
            throw new UsernameNotFoundException("User not found");
        }
        var jwtToekn = jwtService.generateAccessToken(user);
        return AuthResponseDto.builder()
                .token(jwtToekn)
                .build();
    }

    public AuthResponseDto register(RegistrationDto registrationDto) {
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RuntimeException("User with such email is already registered");
        }

        var newUser = UserEntity.builder()
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .email(registrationDto.getEmail())
                .phone(registrationDto.getPhone())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .build();

        userRepository.save(newUser);

        var jwtToken = jwtService.generateAccessToken(newUser);

        return AuthResponseDto.builder()
                .token(jwtToken)
                .build();
    }
}
