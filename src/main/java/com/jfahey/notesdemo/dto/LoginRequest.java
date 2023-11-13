package com.jfahey.notesdemo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

public class LoginRequest {
    
    @NotNull
    @Length(max = 30)
    private String username;
     
    @NotNull 
    @Length(min = 5, max = 50)
    private String password;

    public LoginRequest() { }

    public LoginRequest(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
