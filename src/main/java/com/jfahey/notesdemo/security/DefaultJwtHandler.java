package com.jfahey.notesdemo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.jfahey.notesdemo.dto.JwtLoginResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DefaultJwtHandler implements IJwtHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultJwtHandler.class);


    @Override
    public String getTokenFromRequest(HttpServletRequest request){

        String bearerToken = request.getHeader("Authorization");

        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7, bearerToken.length());
        }

        return null;
    }

    @Override
    public ResponseEntity<?> getLoginResponseFromToken(String token, Authentication auth) {

        JwtLoginResponse response = new JwtLoginResponse(
            //((UserDetails) auth.getPrincipal()).getUsername(), 
            auth.getName(),
            token);

        return ResponseEntity.ok().body(response);
    }

    @Override
    public ResponseEntity<?> handleLogoutResponse(HttpServletResponse resp) {
        return ResponseEntity
            .ok()
            .body("You've been successfully signed out.");
    }
}
