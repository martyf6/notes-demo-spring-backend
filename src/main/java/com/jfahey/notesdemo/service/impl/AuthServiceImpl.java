package com.jfahey.notesdemo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jfahey.notesdemo.dto.LoginRequest;
import com.jfahey.notesdemo.dto.RegisterRequest;
import com.jfahey.notesdemo.model.User;
import com.jfahey.notesdemo.repository.UserRepository;
import com.jfahey.notesdemo.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService{

    @Autowired
    private UserRepository userRepository;
    
    @Autowired 
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public Authentication login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword())
        );
            
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    @Override
    public boolean isRegisteredUsername(String username){
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isRegisteredEmail(String email){
        return userRepository.existsByEmail(email);
    }

    @Override
    public void register(RegisterRequest registerRequest){
        User user = new User(
            registerRequest.getUsername(),
            registerRequest.getEmail(),
            encodePassword(registerRequest.getPassword())
        );

        userRepository.save(user);
    }

    private String encodePassword(String password){
        return encoder.encode(password);
    }

    @Override
    public Authentication getCurrentAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
