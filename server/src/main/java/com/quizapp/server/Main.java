package com.quizapp.server;

import com.quizapp.server.network.ServerBootstrap;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Server started");
        new ServerBootstrap().start();
    }
}
