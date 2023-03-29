package com.encora.chat.service.impl;

import com.encora.chat.model.Message;
import com.encora.chat.service.ECSTaskExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;

@Service
@RequiredArgsConstructor
public class ECSTaskExecutorImpl implements ECSTaskExecutor {

    // logger
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ECSTaskExecutorImpl.class);

    private final EcsClient ecsClient;

    public void executeECSTask(Message lastMessage, String gptResponse) {
        String ecsResponse="";
        RunTaskRequest startTaskRequest = null;
        if(lastMessage.getSenderName().equalsIgnoreCase("user1"))
            startTaskRequest = RunTaskRequest.builder()
                    .cluster("devops-genie")
                    .taskDefinition("devops-genie-user-1:2")
                    .build();
        else if(lastMessage.getSenderName().equalsIgnoreCase("user2"))
            startTaskRequest = RunTaskRequest.builder()
                    .cluster("devops-genie")
                    .taskDefinition("devops-genie-user-2:2").build();
        if(startTaskRequest!= null) {
            RunTaskResponse startTaskResponse = ecsClient.runTask(startTaskRequest);
            ecsResponse = startTaskResponse.toString();
        }

        logger.info("ecs response: {}", ecsResponse);
    }
}
