package com.encora.chat.controller;

import com.encora.chat.model.Conversation;
import com.encora.chat.model.ConversationStatus;
import com.encora.chat.model.Message;
import com.encora.chat.model.MessagePayload;
import com.encora.chat.repo.ConversationRepo;
import com.encora.chat.repo.MessageRepo;
import com.encora.chat.service.AWSServiceHandler;
import com.encora.chat.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.transaction.Transactional;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    // logger
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageRepo messageRepo;

    private final String terraformMessage = ". create terraform code only dont give any extra text.";
    private final ConversationRepo conversationRepo;
    private final OpenAIService openAIService;
    private final AWSServiceHandler awsServiceHandler;
    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public MessagePayload receiveMessage(@Payload MessagePayload message) {
        return message;
    }

    @MessageMapping("/private-message")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public MessagePayload recMessage(@Payload MessagePayload messagePayload) {
        Message message = MessagePayload.toMessage(messagePayload);
        /*
        create a new conversation by creating a new instance of the Conversation
         class and setting the senderName property to the name of the sender.
          Set the conversationStatus property to the initial status of the conversation. You can then persist the conversation object to the database using an ORM framework like Hibernate.
         */
        // check if conversation id is there in message
        if (messagePayload.getConversationId() == null) {
            // create a new conversation
            Conversation conversation = new Conversation();
            conversation.setSenderName(message.getSenderName());
            conversation.setMessages(List.of(message));
            conversation.setConversationStatus(ConversationStatus.INITIATED);
            Conversation newConversation = conversationRepo.save(conversation);
            message.setConversation(newConversation);
            handleMessage(message);
        } else {
            Conversation conversation = conversationRepo.findById(messagePayload.getConversationId()).get();
            message.setConversation(conversation);
            handleMessage(message);
        }
        return MessagePayload.toMessagePayload(message);
    }

    private void handleMessage(Message message) {
        Conversation conversation = message.getConversation();
        List<Message> messages = conversation.getMessages();
        if (messages.stream().anyMatch(m->m.getMessage().contains("create") && m.getMessage().contains("ec2"))) {
            awsServiceHandler.handleEC2Creation(conversation, message);
        } else if(messages.stream().anyMatch(m->m.getMessage().contains("create") && m.getMessage().contains("rds"))) {
            awsServiceHandler.handleRDSCreation(conversation, message);
        } else if(messages.stream().anyMatch(m->m.getMessage().contains("create") && m.getMessage().contains("s3"))) {
            awsServiceHandler.handleS3Creation(conversation, message);
        } else if(messages.stream().anyMatch(m->m.getMessage().contains("create") && m.getMessage().contains("ecs"))) {
            awsServiceHandler.handleECSCreation(conversation, message);
        } else if (messages.stream().anyMatch(m->m.getMessage().contains("infrastructure"))) {
            awsServiceHandler.showInfrastructure(message);
        }
        else {
            handleSingleMessageConversation(message);
        }
    }

    private void handleSingleMessageConversation(Message message) {
        String gptResponse = openAIService.callOpenAI(message.getMessage());
        message.setServerMessage(gptResponse);
        MessagePayload responsePayload = MessagePayload.toMessagePayload(message);
        responsePayload.setConversationId(message.getConversation().getId());
        responsePayload.setConversationStatus("COMPLETED");
        simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private",
                responsePayload);
        message.getConversation().setConversationStatus(ConversationStatus.COMPLETED);
        messageRepo.save(message);
    }


}
