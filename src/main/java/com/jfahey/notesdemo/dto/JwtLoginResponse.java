package com.jfahey.notesdemo.dto;

public class JwtLoginResponse {
    
    private String username;
    private String accessToken;
    private String tokenType = "Bearer";

    public JwtLoginResponse() {}

    public JwtLoginResponse(String username, String accessToken){
        this.username = username;
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
