package com.encora.chat.listener;

import com.encora.chat.model.Conversation;
import com.encora.chat.model.Message;
import com.encora.chat.model.MessagePayload;
import com.encora.chat.repo.ConversationRepo;
import com.encora.chat.repo.MessageRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class ECSStatusListener {

    // create logger for this class
    private static final Logger logger = LoggerFactory.getLogger(ECSStatusListener.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;
    private final MessageRepo messageRepo;
    private final ConversationRepo conversationRepo;


    @SqsListener(value = "Devops_genie", deletionPolicy = SqsMessageDeletionPolicy.ALWAYS)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void listen(String message) {
        logger.info("Message received: {}", message);
        TFStatusMessage tfStatusMessage = null;
        try {
            tfStatusMessage = objectMapper.readValue(message, TFStatusMessage.class);

            MessagePayload payload = MessagePayload.builder()
                    .senderName(tfStatusMessage.getUser())
                    .message("")
                    .serverMessage(tfStatusMessage.getMessage()).build();
            simpMessagingTemplate.convertAndSendToUser(tfStatusMessage.getUser(), "/private", payload);
            // message payload to message
            Conversation conversation = new Conversation();
            conversation.setConversationStatus("COMPLETED");

            Message toPersist = MessagePayload.toMessage(payload);
            Conversation newConversation = conversationRepo.save(conversation);
            toPersist.setConversation(newConversation);
            messageRepo.save(toPersist);
            logger.info("Message received: {}", payload);
        } catch (JsonProcessingException e) {
            logger.error("Error while parsing message: {}", message, e);
        }
    }
}
