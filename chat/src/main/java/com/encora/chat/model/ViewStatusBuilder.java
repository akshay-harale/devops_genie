package com.encora.chat.model;

import java.util.function.Consumer;

/**
 * Created by akshay on 03/05/20
 *
 * @author akshay
 * ViewStatusBuilder
 */
public class ViewStatusBuilder {
    String videoName;
    Long userId;
    String duration;

    public ViewStatusBuilder with(Consumer<ViewStatusBuilder> builderFunction){
        builderFunction.accept(this);
        return this;
    }
    public ViewStatus build() {
        return new ViewStatus(
                this.videoName,
                this.userId,
                this.duration
        );
    }
}
