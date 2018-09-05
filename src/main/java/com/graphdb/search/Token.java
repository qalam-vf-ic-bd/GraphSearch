package com.graphdb.search;

import com.graphdb.utils.Constants;

/**
 * Created by mishkat on 7/18/17.
 */
public class Token {
    private String token;
    private String type;
    private String original;

    private Token(String token) {
        this(token, Constants.KEYWORD_MARK);
    }

    private Token(String token, String type) {
        setToken(token);
        setType(type);
    }

    public static Token buildToken(String token) {
        return new Token(token);
    }

    public static Token buildToken(String token, String type) {
        return new Token(token, type);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void updateToken(String token) {
        if (this.token != null) {
            this.original = this.token;
        }
        setToken(token);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isKeyword() {
        return type.equals(Constants.KEYWORD_MARK);
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    @Override
    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", original='" + original + '\'' +
                '}';
    }
}
