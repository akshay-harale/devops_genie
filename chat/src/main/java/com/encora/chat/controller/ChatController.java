package com.encora.chat.controller;

import com.encora.chat.model.Conversation;
import com.encora.chat.model.ConversationStatus;
import com.encora.chat.model.Message;
import com.encora.chat.model.MessagePayload;
import com.encora.chat.repo.ConversationRepo;
import com.encora.chat.repo.MessageRepo;
import com.encora.chat.service.OpenAIService;
import lombok.RequiredArgsConstructor;
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
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.StartTaskRequest;
import software.amazon.awssdk.services.ecs.model.StartTaskResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    // logger
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageRepo messageRepo;

    private final S3Client s3Client;
    private final Ec2Client ec2Client;
    private final String terraformMessage = ". create terraform code only dont give any extra text.";
    private final ConversationRepo conversationRepo;
    private final OpenAIService openAIService;
    private final EcsClient ecsClient;

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
            handleEC2Creation(conversation, message);
        } else if(messages.stream().anyMatch(m->m.getMessage().contains("create") && m.getMessage().contains("rds"))) {

        } else if(messages.stream().anyMatch(m->m.getMessage().contains("create") && m.getMessage().contains("s3"))) {

        } else {
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

    private void handleEC2Creation(Conversation conversation, Message lastMessage) {
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
            logger.info("response: {}", response);
            String imageName = response.images().get(0).name();
            String finalMessage = String.join("", messages.stream().map(Message::getMessage).toList());

            String gptResponse = openAIService.callOpenAI(finalMessage +" with image name "+ imageName + terraformMessage);
            uploadTOS3(conversation.getSenderName(), gptResponse);
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
                executeECSTask(lastMessage, gptResponse);
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

    private void executeECSTask(Message lastMessage, String gptResponse) {
        String ecsResponse="";
        RunTaskRequest startTaskRequest = null;
        if(lastMessage.getSenderName().equalsIgnoreCase("user1"))
            startTaskRequest = RunTaskRequest.builder()
                    .cluster("devops-genie")
                    .taskDefinition("devops-genie-user-1")
                    .build();
        else if(lastMessage.getSenderName().equalsIgnoreCase("user2"))
            startTaskRequest = RunTaskRequest.builder().cluster("devops-genie").taskDefinition("devops-genie-user-2").build();
        if(startTaskRequest!= null) {
            RunTaskResponse startTaskResponse = ecsClient.runTask(startTaskRequest);
            ecsResponse = startTaskResponse.toString();
        }

        logger.info("ecs response: {}", ecsResponse);
    }

    private void uploadTOS3(String senderName, String response) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("terraform-code-poc").key(senderName+"/"+senderName+new Date()+".tf").build();
        s3Client.putObject(putObjectRequest, RequestBody.fromString(response));
    }

}
