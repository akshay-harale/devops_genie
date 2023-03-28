package com.encora.chat.controller;

import com.encora.chat.model.AIRequest;
import com.encora.chat.model.Conversation;
import com.encora.chat.model.ConversationStatus;
import com.encora.chat.model.Message;
import com.encora.chat.model.TextCompletion;
import com.encora.chat.repo.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Controller
@RequiredArgsConstructor
public class ChatController {

    // logger
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageRepo messageRepo;
    private final RestTemplate restTemplate;
    private final S3Client s3Client;
    private final Ec2Client ec2Client;

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public Message receiveMessage(@Payload Message message) {
        return message;
    }

    @MessageMapping("/private-message")
    public Message recMessage(@Payload Message message) {
        message.setDate(java.time.LocalDateTime.now().toString());
        if(message.getConversation() == null) {
            // consider this as new conversation
            Conversation conversation = new Conversation();
            conversation.setConversationStatus(ConversationStatus.IN_PROGRESS);
            message.setConversation(conversation);
        }
        if(message.getMessage().toLowerCase().contains("create") &&
                message.getMessage().toLowerCase().contains("ec2") || message.getMessage().toLowerCase().contains("server")) {
            if( message.getMessage().toLowerCase().contains("ubuntu")) {
                DescribeImagesRequest ubuntuRequests = DescribeImagesRequest.builder().filters(Filter.builder()
                        .name("ubuntu")
                        .build()).build();
                DescribeImagesResponse describeImagesResponse = ec2Client.describeImages(ubuntuRequests);
                String awsImageId = describeImagesResponse.images().get(0).imageId();
                message.setMessage(message.getMessage()+" with ami id "+awsImageId);
                Conversation conversation = message.getConversation();
                conversation.setConversationStatus(ConversationStatus.COMPLETED);
                message.setConversation(conversation);
                messageRepo.save(message);
                String response = callOpenAI(message.getMessage());
                uploadTOS3(message, response);
                message.setServerMessage("Your request is in progress. Please wait for a while.");
                simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private", message);
                return message;
            } else if ( !message.getMessage().toLowerCase().contains("type")) {
                message.setServerMessage("add instance type in your request. e.g <'your request'> with instance type <'t2.micro'>");
                simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private", message);
                return message;
            } else if ( !message.getMessage().toLowerCase().contains("security group")) {
                message.setServerMessage("add security group in your request. e.g <'your request'> with security group <'sg-0b0b0b0b0b0b0b0b0'>");
                simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private", message);
                return message;
            } else {
                String response = callOpenAI(message.getMessage());
                uploadTOS3(message, response);
                message.setServerMessage("Your request is in progress. Please wait for a while.");
                simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private", message);
                return message;
            }
        } else {
            message.setServerMessage("Sorry, I don't understand your request. please start new conversation");
        }
        simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private", message);
        return message;
    }

    private void uploadTOS3(Message message, String response) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("terraform-code-poc").key(message.getSenderName()+"/"+"fileName.tf").build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(response));
    }

    // method to use rest template and call "https://api.openai.com/v1/chat/completions";
    public String callOpenAI(String message) {
        String url = "https://api.openai.com/v1/completions";
        String apiKey = "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";
        String authorizationHeader = "Bearer " + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorizationHeader);

        AIRequest aiRequest = new AIRequest();
        aiRequest.setModel("text-davinci-003");
        aiRequest.setMax_tokens(1000);
        aiRequest.setTemperature(0.7);
        aiRequest.setPrompt(message);
        aiRequest.setTop_p(1.0);
        aiRequest.setFrequency_penalty(0.0);
        aiRequest.setPresence_penalty(0.0);
        // pojo for above json

        HttpEntity<AIRequest> entity = new HttpEntity<>(aiRequest, headers);
        ResponseEntity<TextCompletion> response = restTemplate.postForEntity(url, entity, TextCompletion.class);
        TextCompletion body = response.getBody();
        logger.info("Response:{}", response);

        return body.getChoices().get(0).getText();
    }
}
