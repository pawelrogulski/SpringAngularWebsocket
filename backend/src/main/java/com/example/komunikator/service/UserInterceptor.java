package com.example.komunikator.service;

import com.example.komunikator.domain.MessageUser;
import com.example.komunikator.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayList;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UserInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {//verifies JWT token during connection
            Map headers = (LinkedMultiValueMap)message.getHeaders().get(SimpMessageHeaderAccessor.NATIVE_HEADERS);
            ArrayList tokenList = (ArrayList)headers.get("token");
            String token = tokenList.get(0).toString();
            if(!jwtUtils.validateJwtToken(token)){
                return null;
            }
            accessor.setUser(new MessageUser(jwtUtils.getUserNameFromJwtToken(token)));
        }
        return message;
    }
}