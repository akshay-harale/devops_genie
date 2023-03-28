package com.encora.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private String model;
    private String prompt;
    private int max_tokens;
    private double temperature;
    private double top_p;
    private double frequency_penalty;
    private double presence_penalty;
}
