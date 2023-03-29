package com.encora.chat.listener;

import com.encora.chat.model.MessagePayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ECSStatusListener {

    // create logger for this class
    private static final Logger logger = LoggerFactory.getLogger(ECSStatusListener.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;
    @SqsListener(value = "Devops_genie", deletionPolicy = SqsMessageDeletionPolicy.ALWAYS)
    public void listen(String message) {
        logger.info("Message received: {}", message);
        TFStatusMessage tfStatusMessage = null;
        try {
            tfStatusMessage = objectMapper.readValue(message, TFStatusMessage.class);

            MessagePayload payload = MessagePayload.builder()
                    .senderName(tfStatusMessage.getUser())
                    .message(tfStatusMessage.getMessage()).build();
            simpMessagingTemplate.convertAndSendToUser(tfStatusMessage.getUser(), "/private", payload);
            logger.info("Message received: {}", payload);
        } catch (JsonProcessingException e) {
            logger.error("Error while parsing message: {}", message, e);
        }
    }
}
