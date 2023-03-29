package com.encora.chat.listener;

import lombok.Data;

@Data
public class TFStatusMessage {
    private String status;
    private String message;
    private String user;
}
