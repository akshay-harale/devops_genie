package com.encora.chat.service;

import com.encora.chat.model.Conversation;
import com.encora.chat.model.Message;

public interface AWSServiceHandler {
    void handleEC2Creation(Conversation conversation, Message lastMessage);

    void handleS3Creation(Conversation conversation, Message message);

    void handleECSCreation(Conversation conversation, Message message);

    void handleRDSCreation(Conversation conversation, Message message);

    void showInfrastructure(Message message);
}
