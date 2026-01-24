package com.secure.appointment.controller;

import com.secure.appointment.dto.request.LoginRequest;
import com.secure.appointment.dto.request.SignupRequest;
import com.secure.appointment.dto.response.JwtResponse;
import com.secure.appointment.dto.response.MessageResponse;
import com.secure.appointment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for User Registration and Login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "User Login",
        description = """
            ### 1. HUMAN SUMMARY
            This endpoint allows a registered user (Admin, Provider, or Client) to log in to the system.
            It validates the credentials and issues a secure access token (JWT).
            
            ### 2. REAL-WORLD SCENARIO
            Sarah, a patient, wants to book an appointment. She opens the app, enters her email 'sarah@example.com' and password, and clicks 'Login'. 
            The system checks her password and, if correct, gives her a digital 'key' (token) to access her dashboard.
            
            ### 3. REQUEST EXPLANATION
            - **Source**: Request Body (JSON)
            - **Required**: Yes
            - **Fields**:
                - `email`: The email address used during registration.
                - `password`: The user's secret password.
            
            ### 4. AUTH SECTION
            - **No Authentication Required**: This is a public endpoint.
            
            ### 5. RESPONSE GUIDE
            - **200 OK**: Login successful.
                - Returns a JSON object with:
                    - `token`: The JWT string (Header.Payload.Signature).
                    - `type`: "Bearer"
                    - `id`, `username`, `email`: User details.
                    - `roles`: List of assigned roles (ROLE_USER, etc.).
            - **401 Unauthorized**: Invalid email or password.
            
            ### 6. ERROR DIAGNOSIS
            - **"Bad credentials"**: You likely typed the wrong password or email. Check for typos.
            """
    )
    @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = JwtResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @Operation(
        summary = "User Registration",
        description = """
            ### 1. HUMAN SUMMARY
            This endpoint creates a new user account in the system. 
            It saves the user's name, email, and password, and assigns them a default role (usually USER/CLIENT).
            
            ### 2. REAL-WORLD SCENARIO
            John is a new doctor who wants to join the platform. He visits the signup page, enters his details, selects 'Provider' role, and clicks 'Register'.
            The system creates his account so he can later log in.
            
            ### 3. REQUEST EXPLANATION
            - **Source**: Request Body (JSON)
            - **Required**: Yes
            - **Fields**:
                - `username`: A unique display name (3-20 characters).
                - `email`: A valid email address (must be unique).
                - `password`: Strong password (min 6 characters).
                - `role`: Optional. Set to ["admin"], ["mod"], or ["user"]. Defaults to "user" if empty.
            
            ### 4. AUTH SECTION
            - **No Authentication Required**: Anyone can register.
            
            ### 5. RESPONSE GUIDE
            - **200 OK**: Registration successful.
                - Returns: `{"message": "User registered successfully!"}`
            - **400 Bad Request**: 
                - Email is already in use.
                - Username is already taken.
                - Validation failed (e.g., password too short).
            
            ### 6. ERROR DIAGNOSIS
            - **"Error: Email is already in use!"**: You have already registered with this email. Try logging in instead.
            - **"Validation Error"**: Your password might be too short, or email format is wrong.
            """
    )
    @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed or User exists")
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
