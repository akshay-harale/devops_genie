package com.encora.chat.model;

/**
 * Created by akshay on 05/05/20
 *
 * @author akshay
 * UserContentDto
 */
public class UserContentDto {
    Integer id;
    String fileName;

    public UserContentDto() {

    }
    public Integer getId() {
        return id;
    }

    public UserContentDto setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public UserContentDto setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
}
