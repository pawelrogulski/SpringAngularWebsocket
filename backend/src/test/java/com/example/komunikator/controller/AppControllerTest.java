package com.example.komunikator.controller;

import com.example.komunikator.domain.User;
import com.example.komunikator.service.AppService;
import com.example.komunikator.service.data.IdUsername;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AppControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    AppController mockedAppController;
    @Mock
    AppService mockedAppService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void usersListNotLoginRedirectAndIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/app/add_friend")).andExpect(status().isUnauthorized());
    }

    @Test
    void findPeopleToChatNotLoginRedirectAndIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/app/conversation/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void userIdInfoIsUnauthorized() throws Exception{
        mockMvc.perform(get("/api/app/user/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void testRegistrationPrecess() throws Exception{
        User user = new User(1,"1","1",null,null);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/api/app/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(roles = "USER")
    void returnUserListWithoutPrincipal() throws Exception {
        User principal = new User(1,"principal","password",null,null);
        User recipient = new User(2,"recipient","password",null,null);
        IdUsername recipientIdUsername = new IdUsername(2,"recipient");
        List<User> userList = new ArrayList<>();
        List<IdUsername> idUsernameList = new ArrayList<>();
        userList.add(principal);
        userList.add(recipient);
        idUsernameList.add(recipientIdUsername);
        when(mockedAppService.getPrincipalUsername(any())).thenReturn("principal");
        when(mockedAppService.getAllUsers()).thenReturn(userList);
        //assertEquals(mockedAppController.usersList(),idUsernameList);
        mockMvc.perform(get("/api/app/add_friend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idUsernameList)))
                .andExpect(status().isOk());
    }
}