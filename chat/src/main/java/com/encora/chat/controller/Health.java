package com.encora.chat.controller;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Health {
    private String status;
}
