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
    private final String terraformMessage = ". create terraform code only dont give any extra text.";
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
        boolean containsRequiredToken = messages.stream().anyMatch(
                message -> message.getMessage().contains("type")) && messages.stream().anyMatch(
                message ->message.getMessage().contains("security group"));
        if (containsRequiredToken) {
            DescribeImagesRequest request = DescribeImagesRequest.builder()
                    .filters( Filter.builder().name("name").values("ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*").build())
                    .build();
            DescribeImagesResponse response = ec2Client.describeImages(request);
            String imageName = response.images().get(0).imageId();
            String finalMessage = String.join("", messages.stream().map(Message::getMessage).toList());

            String gptResponse = openAIService.callOpenAI(finalMessage +" with image name "+ imageName + terraformMessage);
            s3Service.uploadTOS3(conversation.getSenderName(), gptResponse);
            lastMessage.setServerMessage("Your request is being processed");
            MessagePayload responsePayload = MessagePayload.toMessagePayload(lastMessage);
            responsePayload.setConversationId(conversation.getId());
            simpMessagingTemplate.convertAndSendToUser(lastMessage.getSenderName(), "/private",
                    responsePayload);
            conversation.setConversationStatus(ConversationStatus.COMPLETED);
            conversationRepo.save(conversation);
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
            if( !lastMessage.getMessage().contains("security group") ) {
                lastMessage.setServerMessage("Please send the security group of ec2 instance you want to attach" +
                        "in format like security group: sg-1234567890abcdef0");
                simpMessagingTemplate.convertAndSendToUser(lastMessage.getSenderName(), "/private",
                        MessagePayload.toMessagePayload(lastMessage));
                messageRepo.save(lastMessage);
                return;
            }
        }

    }

    @Override
    public void handleS3Creation(Conversation conversation, Message message) {

    }

    @Override
    public void handleECSCreation(Conversation conversation, Message message) {

    }

    @Override
    public void handleRDSCreation(Conversation conversation, Message message) {

    }

}
