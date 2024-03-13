package com.example.komunikator.controller;

import com.example.komunikator.domain.User;
import com.example.komunikator.payload.request.LoginRequest;
import com.example.komunikator.repository.UserRepo;
import com.example.komunikator.service.AppService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTest {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AppService appService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @BeforeAll
    public void addTestUser(){
        User user = new User(1,"username","password",null,null);
        appService.addUser(user);
    }

    @Test
    void authenticateUserTestWithExistedCredentials()  throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("username");
        loginRequest.setPassword("password");
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void authenticateUserTestWithWrongPassword()  throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("username");
        loginRequest.setPassword("wrong_password");
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void authenticateUserTestWithWrongUsername()  throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrong_username");
        loginRequest.setPassword("password");
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void authenticateUserTestWithWrongUsernameAndPassword()  throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrong_username");
        loginRequest.setPassword("wrong_password");
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}