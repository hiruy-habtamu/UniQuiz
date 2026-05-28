package com.quizapp.server;

import com.quizapp.server.network.ServerBootstrap;

public class Main {
    public static void main(String[] args) throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        System.out.println("Server started");
        serverBootstrap.start();
    }
}
