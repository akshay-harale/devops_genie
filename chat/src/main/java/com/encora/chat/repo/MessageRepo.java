package com.encora.chat.repo;

import com.encora.chat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {
    List<Message> findAllBySenderName(String userName);
}
