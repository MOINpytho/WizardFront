package com.example.DataWizard.controller;

import com.example.DataWizard.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin("*")
public class ChatController {

    @Autowired
    ChatService chatService;

    @GetMapping
    public Flux<String> getResponse(@RequestParam(value="inputText",required = true)String inputText) throws IOException {
        Flux<String> response = chatService.generateResponse(inputText);
        return response;
    }
}
