package com.encora.chat.repo;

import com.encora.chat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {
}
