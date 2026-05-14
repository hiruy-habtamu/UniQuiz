package com.quizapp.client.network;

import com.quizapp.shared.message.Message;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5050;

    private final String host;
    private final int port;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public ServerConnection() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void connect() throws IOException {
        if (isConnected()) {
            return;
        }

        socket = new Socket(host, port);
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
    }

    public synchronized Message send(Message message) throws IOException, ClassNotFoundException {
        connect();
        output.writeObject(message);
        output.flush();
        Object response = input.readObject();
        if (response instanceof Message typedResponse) {
            return typedResponse;
        }
        throw new IOException("Received non-message response from server.");
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void close() throws IOException {
        closeQuietly(input);
        closeQuietly(output);
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        input = null;
        output = null;
        socket = null;
    }

    private void closeQuietly(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }
}
