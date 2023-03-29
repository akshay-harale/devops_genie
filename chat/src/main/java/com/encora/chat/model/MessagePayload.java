package com.encora.chat.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class MessagePayload {
    private int id;
    private String senderName;
    private String receiverName;
    private String message;
    private String serverMessage;
    private String date;
    private Status status;
    private Long conversationId;
    private String conversationStatus;

    public static MessagePayload toMessagePayload(Message message) {
        return MessagePayload.builder()
                .id(message.getId())
                .senderName(message.getSenderName())
                .receiverName(message.getReceiverName())
                .message(message.getMessage())
                .serverMessage(message.getServerMessage())
                .date(message.getDate())
                .status(message.getStatus())
                .conversationStatus(message.getConversation().getConversationStatus())
                .conversationId(message.getConversation().getId())
                .build();
    }

    public static Message toMessage(MessagePayload message) {
        return Message.builder()
                .id(message.getId())
                .senderName(message.getSenderName())
                .receiverName(message.getReceiverName())
                .message(message.getMessage())
                .serverMessage(message.getServerMessage())
                .date(message.getDate())
                .status(message.getStatus())
                .build();
    }
}
