package com.encora.chat.model;

import java.io.Serializable;

/**
 * Created by akshay on 03/05/20
 *
 * @author akshay
 * ViewStatus
 */
public class ViewStatusDto implements Serializable {


    String videoName;

    Long userId;

    String duration;



    public ViewStatusDto(String videoName, Long userId, String duration) {
        this.videoName = videoName;
        this.userId = userId;
        this.duration = duration;
    }

    public String getVideoName() {
        return videoName;
    }

    public ViewStatusDto setVideoName(String videoName) {
        this.videoName = videoName;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public ViewStatusDto setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public String getDuration() {
        return duration;
    }

    public ViewStatusDto setDuration(String duration) {
        this.duration = duration;
        return this;
    }
}
