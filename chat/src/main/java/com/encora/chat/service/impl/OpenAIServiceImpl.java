package com.encora.chat.service.impl;

import com.encora.chat.model.AIRequest;
import com.encora.chat.model.TextCompletion;
import com.encora.chat.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class OpenAIServiceImpl implements OpenAIService {

    // create logger for this class
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OpenAIServiceImpl.class);

    private final RestTemplate restTemplate;
    @Override
    public String callOpenAI(String message) {
        String url = "https://api.openai.com/v1/completions";
        String apiKey = "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";
        String authorizationHeader = "Bearer " + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorizationHeader);

        AIRequest aiRequest = new AIRequest();
        aiRequest.setModel("text-davinci-003");
        aiRequest.setMax_tokens(2049);
        aiRequest.setTemperature(0.7);
        aiRequest.setPrompt(message);
        aiRequest.setTop_p(1.0);
        aiRequest.setFrequency_penalty(0.0);
        aiRequest.setPresence_penalty(0.0);
        // pojo for above json

        HttpEntity<AIRequest> entity = new HttpEntity<>(aiRequest, headers);
        ResponseEntity<TextCompletion> response = restTemplate.postForEntity(url, entity, TextCompletion.class);
        TextCompletion body = response.getBody();
        logger.info("Response:{}", response);

        return body.getChoices().get(0).getText();
    }
}
