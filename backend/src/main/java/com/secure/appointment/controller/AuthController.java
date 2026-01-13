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
/**
 * AuthController: The Entry Gate for Authentication.
 * 
 * What it does:
 * This controller handles "Login" and "Registration" requests coming from the Frontend.
 * It acts as the doorman: checking who is trying to enter and if they have the right credentials.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Function: authenticateUser (Login)
     * 
     * 1. FRONTEND TRIGGER: 
     *    User fills the Login Form (Email, Password) on the React Frontend and clicks "Login".
     *    React sends a POST request to 'http://localhost:8080/api/auth/login' with the data.
     * 
     * 2. DATA SOURCE:
     *    The 'LoginRequest' object contains the email and password sent by the React app.
     *    @RequestBody tells Spring to map the JSON data from the frontend into this Java object.
     * 
     * 3. LOGIC (What happens here):
     *    - It calls 'authService.authenticateUser()' to verify the password.
     *    - If password matches, it generates a JWT (JSON Web Token), which is like a digital ID card.
     * 
     * 4. OUTCOME:
     *    - Returns a 'JwtResponse' containing the Token and User Details.
     *    - The Frontend receives this, saves the Token in 'localStorage', and lets the user access the app.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    /**
     * Function: registerUser (Signup)
     * 
     * 1. FRONTEND TRIGGER:
     *    User fills the Registration Form (Name, Email, Password, Role) and clicks "Register".
     *    React sends a POST request to 'http://localhost:8080/api/auth/register'.
     * 
     * 2. DATA SOURCE:
     *    The 'SignupRequest' object contains the new user's details.
     * 
     * 3. LOGIC:
     *    - First, it checks for validation errors (e.g., bad email format, short password).
     *    - If valid, it calls 'authService.registerUser()' to save the new user to the database.
     * 
     * 4. OUTCOME:
     *    - If successful, returns "User registered successfully!".
     *    - If failed (e.g., email exists), returns an error message.
     */
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
