package com.encora.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public interface S3Service {
    void uploadTOS3(String senderName, String response);
}
