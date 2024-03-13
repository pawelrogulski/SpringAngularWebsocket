package com.example.komunikator.controller;

import com.example.komunikator.domain.User;
import com.example.komunikator.service.AppService;
import com.example.komunikator.service.data.Chat;
import com.example.komunikator.service.data.IdUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/app")
public class AppController {
    private final AppService appService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat")
    public void send(@Payload String recipientAndMessage, Principal principal) {
        String sendFrom ="";
        try{sendFrom = principal.getName();}
        catch (NullPointerException e){
            System.out.println("Not authorized user");
        }
        // message consists of 3 elements: recipient's login, space, and content
        int space = recipientAndMessage.indexOf(" "); //space
        String sendTo = recipientAndMessage.substring(0,space); //login
        String message = recipientAndMessage.substring(space+1,recipientAndMessage.length()); //content
        int conversationId = appService.findConversation(appService.getUserByUsername(sendFrom).getId(),appService.getUserByUsername(sendTo).getId());
        appService.addMessage(message,appService.getUserByUsername(sendFrom).getId(),conversationId); //save in DB
        simpMessagingTemplate.convertAndSendToUser(sendTo, "/queue/messages", sendFrom+": "+message); //send by WebSocket to recipient
    }

    @PostMapping("/register")
    public User processRegister(@RequestBody User user) {
        return appService.addUser(user);
    }
    @GetMapping("/add_friend")
    public List<IdUsername> usersList(){
        List<User> users = appService.getAllUsers();
        String username = appService.getPrincipalUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return users.stream()  //block sending messages to yourself
                .filter(user -> !user.getUsername().equals(username))
                .map(user -> new IdUsername(user.getId(),user.getUsername()))//DTO
                .collect(Collectors.toList());
    }

    @GetMapping("/conversation/{id}")//{id} is recipient's id
    public ResponseEntity<Chat> ChatTo(@PathVariable String id){
        String username = appService.getPrincipalUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok(new Chat(appService.sortMessagesByUsername(appService.getConversationById(Integer.parseInt(id))),
                appService.findRecipientUsername(Integer.parseInt(id),username),
                username));
    }

    @GetMapping("user/{id}")//id of one of the conversation users; the ID of the logged-in user initiating the conversation is retrieved from the principal
    public int getConversationId(@PathVariable String id){
        String username = appService.getPrincipalUsername(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return appService.startConversation(appService.getUserByUsername(username).getId(),Integer.parseInt(id));
    }
}