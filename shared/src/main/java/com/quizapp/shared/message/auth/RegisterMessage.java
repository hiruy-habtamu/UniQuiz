package com.quizapp.shared.message.auth;

import com.quizapp.shared.message.Message;

public class RegisterMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final String fullName;
    private final String role;
    private final Integer batchId;

    public RegisterMessage(String username, String password,
                           String fullName, String role, Integer batchId) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role     = role;
        this.batchId  = batchId;
    }

    public String  getUsername() { return username; }
    public String  getPassword() { return password; }
    public String  getFullName() { return fullName; }
    public String  getRole()     { return role;     }
    public Integer getBatchId()  { return batchId;  }
}