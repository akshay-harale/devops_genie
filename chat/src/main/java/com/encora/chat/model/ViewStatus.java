package com.encora.chat.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by akshay on 03/05/20
 *
 * @author akshay
 * ViewStatus
 */
@Entity
@Table(name = "view_status")
public class ViewStatus implements Serializable {

    @Column
    String videoName;
    @Column
    Long userId;
    @Column
    String duration;

    @EmbeddedId
    private ViewStatusKey key;


    public ViewStatus(){

    }

    public ViewStatusKey getKey() {
        return key;
    }

    public ViewStatus setKey(ViewStatusKey key) {
        this.key = key;
        return this;
    }

    public ViewStatus(String videoName, Long userId, String duration) {
        this.videoName = videoName;
        this.userId = userId;
        this.duration = duration;
    }

    public String getVideoName() {
        return videoName;
    }

    public ViewStatus setVideoName(String videoName) {
        this.videoName = videoName;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public ViewStatus setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public String getDuration() {
        return duration;
    }

    public ViewStatus setDuration(String duration) {
        this.duration = duration;
        return this;
    }
}
