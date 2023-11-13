package com.jfahey.notesdemo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.util.WebUtils;

import com.jfahey.notesdemo.dto.JwtLoginResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieJwtHandler implements IJwtHandler  {

    @Value("${app.jwt.cookieName}")
    private String jwtCookie;

    private int jwtCookieAge = 24 * 60 * 60;

    @Override
    public String getTokenFromRequest(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    @Override
    public ResponseEntity<?> getLoginResponseFromToken(String token, Authentication auth) {
        
        ResponseCookie cookie = ResponseCookie
            .from(jwtCookie, token)
            .path("/auth")
            .maxAge(jwtCookieAge)
            .httpOnly(true)
            .build();

        JwtLoginResponse response = new JwtLoginResponse(
            //((UserDetails) auth.getPrincipal()).getUsername(), 
            auth.getName(),
            token);

        return ResponseEntity
            .ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(response);
    }

    @Override
    public ResponseEntity<?> handleLogoutResponse(HttpServletResponse resp) {
        ResponseCookie cookie = ResponseCookie
            .from(jwtCookie, null)
            .path("/auth")
            .build();

        resp.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity
            .ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body("You've been successfully signed out.");
    }
    
}
