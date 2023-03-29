package com.encora.chat.service;

import org.springframework.stereotype.Service;


public interface OpenAIService {
    String callOpenAI(String message);
}
