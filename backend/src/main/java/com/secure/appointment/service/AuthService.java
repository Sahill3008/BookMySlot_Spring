package com.secure.appointment.service;

import com.secure.appointment.dto.request.LoginRequest;
import com.secure.appointment.dto.request.SignupRequest;
import com.secure.appointment.dto.response.JwtResponse;
import com.secure.appointment.entity.User;
import com.secure.appointment.repository.UserRepository;
import com.secure.appointment.security.CustomUserDetails;
import com.secure.appointment.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService: The Logic Center for Authentication
 * 
 * What it does:
 * This service contains the "Business Logic" for logging in and registering.
 * Controllers are just messengers; this class does the actual heavy lifting.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Function: authenticateUser (Login Logic)
     * 
     * 1. TRIGGER: Called by AuthController.authenticateUser()
     * 
     * 2. LOGIC:
     *    a. authenticationManager.authenticate(): 
     *       - Checks against the Database if Email exists and Password matches.
     *       - This is where Spring Security does its magic.
     *    b. SecurityContextHolder...setAuthentication(): 
     *       - Tells the system "This user is now logged in" for the current request.
     *    c. jwtUtils.generateToken(user): 
     *       - Creates a secure text string (JWT) that proves who the user is.
     * 
     * 3. OUTCOME:
     *    - Returns a standardized 'JwtResponse' object back to the controller.
     */
    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String jwt = jwtUtils.generateToken(user);

        return new JwtResponse(jwt,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name());
    }

    /**
     * Function: registerUser (Signup Logic)
     * 
     * 1. TRIGGER: Called by AuthController.registerUser()
     * 
     * 2. LOGIC:
     *    a. Check if Email exists: We can't have duplicate emails.
     *    b. User Builder: Creates a NEW User entity.
     *    c. passwordEncoder.encode(): 
     *       - CRITICAL: We NEVER save plain text passwords (like "123456").
     *       - We "hash" them into a scrambled string so even admins can't read them.
     *    d. userRepository.save(user): Commits the new user to the PostgreSQL database.
     * 
     * 3. OUTCOME:
     *    - New row added to 'users' table.
     */
    @Transactional
    public void registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(signUpRequest.getRole())
                .isActive(true)
                .build();

        userRepository.save(user);
    }
}
