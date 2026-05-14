package com.quizapp.shared.message.auth;

import com.quizapp.shared.message.Message;

public class LoginMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    public LoginMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
