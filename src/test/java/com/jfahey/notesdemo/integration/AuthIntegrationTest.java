package com.jfahey.notesdemo.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfahey.notesdemo.dto.LoginRequest;
import com.jfahey.notesdemo.dto.RegisterRequest;
import com.jfahey.notesdemo.model.User;
import com.jfahey.notesdemo.repository.UserRepository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void setup(){
        userRepository.deleteAll();
    }

    @Test
    public void givenRegisterRequest_whenSignUp_thenReturnOk() throws Exception {
        
        // give: user registration request
        RegisterRequest registerRequest = new RegisterRequest(
            "user1",
            "user1@domain.com",
            "password1"
        );

        // when: post register request
        ResultActions response = mockMvc.perform(post("/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));

        // then: verify regigstered successfully returned
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("registered successfully")));
    }

    @Test
    public void givenRegisterRequest_whenSignUpWithTakenUsername_thenReturnBadRequest() throws Exception {
        
        // given: existing user and register request with same username
        User user = generateTestUser();

        RegisterRequest registerRequest = new RegisterRequest(
            user.getUsername(),
            "test@domain.com",
            "testpassword"
        );

        // when: post register request with taken username
        ResultActions response = mockMvc.perform(post("/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)));

        // then: verify bad request and returned failure string body
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username is already taken")));
    }

    @Test
    public void givenLoginRequest_whenLogin_thenReturnToken() throws Exception {
        
        // given: existing user
        String username = "user1";
        String password = "password1";
        generateTestUser(username, username + "@domain.com", password);

        LoginRequest loginRequest = new LoginRequest(username, password);

        // when: post login request with correct user credentials
        ResultActions response = mockMvc.perform(post("/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)));

        // then: verify successfully returned user access token
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(loginRequest.getUsername())))
                .andExpect(jsonPath("$.accessToken", is(not(emptyString()))))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    public void givenLoginRequest_whenInvalidLogin_thenReturnUnauthorized() throws Exception {
        
        // given: existing user
        User user = generateTestUser();
        
        LoginRequest loginRequest = new LoginRequest(
            user.getUsername(),
            "wrongpassword"
        );

        // when: post login request with incorrect user credentials
        ResultActions response = mockMvc.perform(post("/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)));

        // then: verify unauthorized returned
        response.andDo(print())
                .andExpect(status().isUnauthorized());
    }

    private User generateTestUser(){
        return generateTestUser("user1", "user1@domain.com", "password1");
    }

    private User generateTestUser(String username, String email, String pw){
        return userRepository.save(new User(
            username,
            email,
            passwordEncoder.encode(pw)
        ));
    }
}
