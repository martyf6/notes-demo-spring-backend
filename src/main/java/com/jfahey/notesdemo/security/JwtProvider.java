package com.jfahey.notesdemo.security;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expirationms}")
    private long jwtExpirationMs;

    private IJwtHandler jwtHandler;

    public JwtProvider(){
        this.jwtHandler = new DefaultJwtHandler();
    }

    public String generateTokenFromAuth(Authentication authentication){

        // TODO: implement customer UserDetails mapper
        User user = (User) authentication.getPrincipal();
        return generateToken(user.getUsername());
    }

    public String generateToken(String subject){
        Date date = new Date();

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(date)
                .setExpiration(new Date(date.getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
        // for userId:
        //return Long.parseLong(claims.getSubject());
      }

    public boolean validateAccessToken(String token) {
        // TODO: throw better exceptions (application specific)
        try {
            Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("JWT expired", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT is empty", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("JWT is invalid", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT is not supported", e.getMessage());
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed", e.getMessage());
        }
         
        return false;
    }

    public String getTokenFromRequest(HttpServletRequest request){
        return jwtHandler.getTokenFromRequest(request);
    }

    public ResponseEntity<?> getLoginResponseFromAuth(Authentication auth){
        String token = generateTokenFromAuth(auth);
        return jwtHandler.getLoginResponseFromToken(token, auth);
    }

    public ResponseEntity<?> handleLogoutResponse(HttpServletResponse resp){
        return jwtHandler.handleLogoutResponse(resp);
    }
}
