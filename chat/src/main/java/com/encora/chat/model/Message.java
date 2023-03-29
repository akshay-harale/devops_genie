package com.encora.chat.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Builder
public class Message {
    // add auto increment id annotation
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private int id;
    private String senderName;
    private String receiverName;
    @Column(length = 10000)
    private String message;
    @Column(length = 10000)
    private String serverMessage;
    private String date;
    private Status status;
    @ManyToOne(targetEntity = Conversation.class)
    @JoinColumn(name = "conversation_id", referencedColumnName = "id")
    @JsonManagedReference
    private Conversation conversation;
}
