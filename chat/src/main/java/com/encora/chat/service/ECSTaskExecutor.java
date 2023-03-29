package com.encora.chat.service;

import com.encora.chat.model.Message;

public interface ECSTaskExecutor {
    void executeECSTask(Message lastMessage, String gptResponse);
}
