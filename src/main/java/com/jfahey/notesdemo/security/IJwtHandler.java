package com.jfahey.notesdemo.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IJwtHandler {

    String getTokenFromRequest(HttpServletRequest request);

    ResponseEntity<?> getLoginResponseFromToken(String token, Authentication auth);

    ResponseEntity<?> handleLogoutResponse(HttpServletResponse resp);
}
