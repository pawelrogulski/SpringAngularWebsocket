package com.example.komunikator.service.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Chat {
    List<String> messages;
    String recipient;
    String username;
}
