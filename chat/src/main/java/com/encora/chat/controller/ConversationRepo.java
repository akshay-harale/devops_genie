package com.encora.chat.controller;

import com.encora.chat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepo extends JpaRepository<Conversation, Integer> {
}
