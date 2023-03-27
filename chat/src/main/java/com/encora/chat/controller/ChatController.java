package com.encora.chat.controller;

import com.encora.chat.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public Message receiveMessage(@Payload Message message){
        return message;
    }

    @MessageMapping("/private-message")
    public Message recMessage(@Payload Message message) throws InterruptedException {
        message.setServerMessage("Server cha message"+message.getSenderName());
        simpMessagingTemplate.convertAndSendToUser(message.getSenderName(),"/private",message);
        System.out.println(message.toString());
        return message;
    }

    // rest controller for sending message to specific user
    @PostMapping("/send")
    public void sendMessage(@RequestBody Message message){
        simpMessagingTemplate.convertAndSendToUser(message.getSenderName(),"/private",message);
    }

}
