package com.secure.appointment.controller;

import com.secure.appointment.dto.request.LoginRequest;
import com.secure.appointment.dto.request.SignupRequest;
import com.secure.appointment.dto.response.JwtResponse;
import com.secure.appointment.dto.response.MessageResponse;
import com.secure.appointment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, org.springframework.validation.BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> sb.append(error.getDefaultMessage()).append("; "));
            System.out.println("Validation Error: " + sb.toString());
            return ResponseEntity.badRequest().body(new MessageResponse("Validation Error: " + sb.toString()));
        }
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
