package com.encora.chat.controller;

import com.encora.chat.model.Message;
import com.encora.chat.repo.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class APIController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final MessageRepo messageRepo;

    @PostMapping("/send")
    public void sendMessage(@RequestBody Message message){
        message.setServerMessage(message.getMessage());
        message.setMessage("");
        simpMessagingTemplate.convertAndSendToUser(message.getSenderName(),"/private",message);
    }

    @GetMapping("/messages/{userName}")
    public List<Message> getMessages(@PathVariable("userName") String userName) {
        return messageRepo.findAllBySenderName(userName);
    }
}
