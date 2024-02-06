package com.jfahey.notesdemo.service;

import org.springframework.security.core.Authentication;

import com.jfahey.notesdemo.dto.LoginRequest;
import com.jfahey.notesdemo.dto.RegisterRequest;

public interface AuthService {

    public Authentication login(LoginRequest request);

    public boolean isRegisteredUsername(String username);

    public boolean isRegisteredEmail(String email);

    public void register(RegisterRequest registerRequest);

    public Authentication getCurrentAuthentication();
}
