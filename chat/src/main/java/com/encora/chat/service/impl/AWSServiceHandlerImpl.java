package com.encora.chat.service.impl;

import com.encora.chat.model.Conversation;
import com.encora.chat.model.ConversationStatus;
import com.encora.chat.model.Message;
import com.encora.chat.model.MessagePayload;
import com.encora.chat.repo.ConversationRepo;
import com.encora.chat.repo.MessageRepo;
import com.encora.chat.service.AWSServiceHandler;
import com.encora.chat.service.ECSTaskExecutor;
import com.encora.chat.service.OpenAIService;
import com.encora.chat.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AWSServiceHandlerImpl implements AWSServiceHandler {
    // create logger for this class
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AWSServiceHandlerImpl.class);

    private final S3Service s3Service;
    private final Ec2Client ec2Client;
    private final String terraformMessage = " create terraform code only, dont give any extra text. Also give sensible names to resources." +
            "dont use provider block in terraform code.";
    private final ConversationRepo conversationRepo;
    private final OpenAIService openAIService;
    // declare simpMessagingTemplate
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageRepo messageRepo;
    // ecs task executor
    private final ECSTaskExecutor ecsTaskExecutor;

    @Override
    public void handleEC2Creation(Conversation conversation, Message lastMessage) {
        // check what is the status of ec2 requirements
        // check for type and security group in list of conversation messages
        List<Message> existingMessages = conversation.getMessages();
        List<Message> messages = new ArrayList<>(existingMessages);
        messages.add(lastMessage);
        boolean containsRequiredToken =
                messages.stream().anyMatch(message -> message.getMessage().contains("type")) ;
        if (containsRequiredToken) {
            DescribeImagesRequest request = DescribeImagesRequest.builder()
                    .filters( Filter.builder().name("name").values("ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*").build())
                    .build();
            DescribeImagesResponse response = ec2Client.describeImages(request);
            logger.info("Describe image response {}", response.images().stream().map(image -> image.imageId()).toList());
            String imageName = response.images().get(0).imageId();
            String userRequest = String.join("", messages.stream().map(Message::getMessage).toList())
                    +" with image name "+ imageName;
            String gptResponse = openAIService.callOpenAI(userRequest + ""  + terraformMessage);
            s3Service.uploadTOS3(conversation.getSenderName(), gptResponse);
            lastMessage.setServerMessage("Your request is being processed");
            MessagePayload responsePayload = MessagePayload.toMessagePayload(lastMessage);
            responsePayload.setConversationId(conversation.getId());
            simpMessagingTemplate.convertAndSendToUser(lastMessage.getSenderName(), "/private",
                    responsePayload);
            conversation.setConversationStatus(ConversationStatus.COMPLETED);
            lastMessage.setConversation(conversation);
            messageRepo.save(lastMessage);
            // start new thread using executor service to call ecs task using ecs client
            // and send the response back to the user
            new Thread(() -> {
                ecsTaskExecutor.executeECSTask(lastMessage, gptResponse);
            }).start();
        } else {
            if( !lastMessage.getMessage().contains("type") ) {
                lastMessage.setServerMessage("Please send the type of ec2 instance you want to create in " +
                        "format like type: t2.micro");
                simpMessagingTemplate.convertAndSendToUser(lastMessage.getSenderName(), "/private",
                        MessagePayload.toMessagePayload(lastMessage));
                messageRepo.save(lastMessage);
                return;
            }
        }

    }

    @Override
    public void handleS3Creation(Conversation conversation, Message message) {
        // check if messages containing bucket name
        List<Message> existingMessages = conversation.getMessages();
        List<Message> messages = new ArrayList<>(existingMessages);
        messages.add(message);
        boolean containsRequiredToken =
                messages.stream().anyMatch(msg -> msg.getMessage().contains("name"));
        if (containsRequiredToken) {
            String userRequest = String.join("", messages.stream().map(Message::getMessage).toList());
            String gptResponse = openAIService.callOpenAI(userRequest + ""  + terraformMessage);
            s3Service.uploadTOS3(conversation.getSenderName(), gptResponse);
            message.setServerMessage("Your request is being processed");
            MessagePayload responsePayload = MessagePayload.toMessagePayload(message);
            responsePayload.setConversationId(conversation.getId());
            simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private",
                    responsePayload);
            conversation.setConversationStatus(ConversationStatus.COMPLETED);
            message.setConversation(conversation);
            messageRepo.save(message);
            // start new thread using executor service to call ecs task using ecs client
            // and send the response back to the user
            new Thread(() -> {
                ecsTaskExecutor.executeECSTask(message, gptResponse);
            }).start();
        } else if( !message.getMessage().contains("name") ) {
                message.setServerMessage("Please send the bucket name you want to create in " +
                        "format like bucket name: mybucket");
                simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private",
                        MessagePayload.toMessagePayload(message));
                messageRepo.save(message);
                return;
        }
    }

    @Override
    public void handleECSCreation(Conversation conversation, Message message) {

    }

    @Override
    public void handleRDSCreation(Conversation conversation, Message message) {
        /**
         * allocated_storage = 20 //required
         * 	identifier = "testinstance"
         * 	storage_type = "gp2"
         *  	engine = "mysql" //required
         *  	engine_version = "5.7"
         *  	instance_class = "db.t2.small" //required
         *   	db_name = "test"
         *   	username = "admin" //required
         *   	password = "Admin54132$" //required
         */
        // validate above fields and send to open ai
        List<Message> existingMessages = conversation.getMessages();
        List<Message> messages = new ArrayList<>(existingMessages);
        messages.add(message);
        boolean containsRequiredToken =
                messages.stream().anyMatch(msg -> msg.getMessage().contains("allocated_storage")) &&
                        messages.stream().anyMatch(msg -> msg.getMessage().contains("engine")) &&
                        messages.stream().anyMatch(msg -> msg.getMessage().contains("instance_class")) &&
                        messages.stream().anyMatch(msg -> msg.getMessage().contains("username")) &&
                        messages.stream().anyMatch(msg -> msg.getMessage().contains("password"));
        if (containsRequiredToken) {
            String userRequest = String.join("", messages.stream().map(Message::getMessage).toList());
            String gptResponse = openAIService.callOpenAI(userRequest + "" +
                    "with skip_final_snapshot = true " +
                    "with apply_immediately = true."  + terraformMessage);
            s3Service.uploadTOS3(conversation.getSenderName(), gptResponse);
            message.setServerMessage("Your request is being processed");
            MessagePayload responsePayload = MessagePayload.toMessagePayload(message);
            responsePayload.setConversationId(conversation.getId());
            simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private",
                    responsePayload);
            conversation.setConversationStatus(ConversationStatus.COMPLETED);
            message.setConversation(conversation);
            messageRepo.save(message);
            // start new thread using executor service to call ecs task using ecs client
            // and send the response back to the user
            new Thread(() -> {
                ecsTaskExecutor.executeECSTask(message, gptResponse);
            }).start();
        } else if( !messages.stream().anyMatch(msg -> msg.getMessage().contains("allocated_storage"))) {
            message.setServerMessage("Please send the allocated storage you want to create in " +
                    "format like allocated_storage: 20");
            simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private",
                    MessagePayload.toMessagePayload(message));
            messageRepo.save(message);
            return;
        } else if( !messages.stream().anyMatch(msg -> msg.getMessage().contains("engine"))) {
            message.setServerMessage("Please send the engine you want to create in " +
                    "format like engine: mysql");
            simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private",
                    MessagePayload.toMessagePayload(message));
            messageRepo.save(message);
            return;
        } else if( !messages.stream().anyMatch(msg -> msg.getMessage().contains("instance_class"))) {
            message.setServerMessage("Please send the instance class you want to create in " +
                    "format like instance_class: db.t2.small");
            simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private",
                    MessagePayload.toMessagePayload(message));
            messageRepo.save(message);
            return;
        } else if( !messages.stream().anyMatch(msg -> msg.getMessage().contains("username"))) {
            message.setServerMessage("Please send the username you want to create in " +
                    "format like username: admin");
            simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private",
                    MessagePayload.toMessagePayload(message));
            messageRepo.save(message);
            return;
        } else if( !messages.stream().anyMatch(msg -> msg.getMessage().contains("password"))) {
            message.setServerMessage("Please send the password you want to create in " +
                    "format like password: Admin54132$");
            simpMessagingTemplate.convertAndSendToUser(message.getSenderName(), "/private",
                    MessagePayload.toMessagePayload(message));
            messageRepo.save(message);
            return;
        }
    }

}
