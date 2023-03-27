package com.encora.chat.model;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by akshay on 03/05/20
 *
 * @author akshay
 * ViewStatusKey
 */
@Embeddable
public class ViewStatusKey implements Serializable {
    @NotNull
    String videoN;
    @NotNull
    Long uId;


    public ViewStatusKey(){

    }
    public ViewStatusKey(@NotNull String videoN, @NotNull Long uId) {
        this.videoN = videoN;
        this.uId = uId;
    }

    public String getVideoN() {
        return videoN;
    }

    public ViewStatusKey setVideoN(String videoN) {
        this.videoN = videoN;
        return this;
    }

    public Long getuId() {
        return uId;
    }

    public ViewStatusKey setuId(Long uId) {
        this.uId = uId;
        return this;
    }
}
