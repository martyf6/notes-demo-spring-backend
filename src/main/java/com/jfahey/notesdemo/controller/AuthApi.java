package com.jfahey.notesdemo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jfahey.notesdemo.dto.LoginRequest;
import com.jfahey.notesdemo.dto.RegisterRequest;
import com.jfahey.notesdemo.security.JwtProvider;
import com.jfahey.notesdemo.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/auth")
public class AuthApi {

    private static final Logger logger = LoggerFactory.getLogger(AuthApi.class);

    @Autowired
    private AuthService authService;
    
    @Autowired 
    private JwtProvider jwtProvider;
     
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {

        try {
            Authentication authentication = authService.login(request);

            return jwtProvider.getLoginResponseFromAuth(authentication);
             
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> signup(@RequestBody @Valid RegisterRequest registerRequest) {

        if(authService.isRegisteredEmail(registerRequest.getEmail())) {

            return ResponseEntity
                .badRequest()
                .body("Email is already in use.");

        } else if (authService.isRegisteredUsername(registerRequest.getUsername())) {

            return ResponseEntity
                .badRequest()
                .body("Username is already taken.");
        }

        authService.register(registerRequest);

        return ResponseEntity.ok().body("User registered successfully.");
    }

    /**
     * Custom Logout Endpoint
     * 
     * While not the recommended method to handle logout functionality with Spring Security
     * (a custom logout handler should likely belong in the security config DSL),
     * this implementation is simply a demonstration of how to configure a logout endpoint.
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response){

        logger.info("Logging out user with custom endpoint.");

        Authentication authentication = authService.getCurrentAuthentication();
        ResponseEntity<?> resp = jwtProvider.handleLogoutResponse(response);

        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return resp;
    }
}
