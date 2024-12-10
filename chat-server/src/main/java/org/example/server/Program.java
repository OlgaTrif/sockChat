package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class Program {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1400);
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}