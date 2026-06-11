package com.library.service;

import com.library.dto.AuthRequestDto;
import com.library.dto.AuthResponseDto;
import com.library.dto.RegisterRequestDto;
import com.library.exception.UserAlreadyExistsException;
import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.security.JwtTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponseDto login(AuthRequestDto request) throws Exception {
        try {
            // authenticate() still works same way in Spring Security 6
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (DisabledException e) {
            throw new Exception("User account is disabled");
        } catch (BadCredentialsException e) {
            throw new Exception("Invalid credentials");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails);

        return new AuthResponseDto(token, request.getUsername());
    }

    public User register(RegisterRequestDto request) {
        if (StringUtils.isBlank(request.getUsername())) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<String> roles = new HashSet<>();
        roles.add("USER");
        user.setRoles(roles);

        return userRepository.save(user);
    }
}
