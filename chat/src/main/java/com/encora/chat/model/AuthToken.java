package com.encora.chat.model;

/**
 * Created by akshay on 02/05/20
 *
 * @author akshay
 * AuthToken
 */
public class AuthToken {

    private String token;
    private String username;

    public AuthToken(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public AuthToken setToken(String token) {
        this.token = token;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AuthToken setUsername(String username) {
        this.username = username;
        return this;
    }
}
