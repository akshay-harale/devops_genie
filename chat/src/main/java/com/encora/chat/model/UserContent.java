package com.encora.chat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by akshay on 05/05/20
 *
 * @author akshay
 * UserContent
 */
@Entity
@Table(name="user_content")
public class UserContent {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    @Column
    String userName;

    @Column
    String contentType;

    @Column
    String fileName;

    @Column
    String contentUrl;

    @Column
    String cloudUrl;

    public UserContent() {

    }

    public int getId() {
        return id;
    }

    public UserContent setId(int id) {
        this.id = id;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public UserContent setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public UserContent setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public UserContent setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public UserContent setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
        return this;
    }

    public String getCloudUrl() {
        return cloudUrl;
    }

    public UserContent setCloudUrl(String cloudUrl) {
        this.cloudUrl = cloudUrl;
        return this;
    }
}
