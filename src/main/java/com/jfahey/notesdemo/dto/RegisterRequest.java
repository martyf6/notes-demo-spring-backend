package com.jfahey.notesdemo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class RegisterRequest {

    @NotNull 
    @Length(min = 5, max = 30)
    private String username;

    @NotNull 
    @Email 
    @Length(min = 5, max = 50)
    private String email;
     
    @NotNull 
    @Length(min = 5, max = 50)
    private String password;

    public RegisterRequest(){}

    public RegisterRequest(String username, String email, String password){
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
