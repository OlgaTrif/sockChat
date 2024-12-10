package org.example.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientManager implements Runnable{

    private final Socket socket;
    public static final List<ClientManager> clients = new ArrayList<>();
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String name;

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println("Клиент " + name + " подключился к чату.");
            broadcastMessage("Server: " + name +  " подключился к чату.");
        } catch (IOException e) {
            closeEverything(this.socket, bufferedWriter, bufferedReader);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(this.socket, bufferedWriter, bufferedReader);
                break;
            }

        }
    }

    private void broadcastMessage(String message) {
        String clientToSendMessage = findClientNameFromMessage(message);
        //отправляем конкретному человеку
        if (clientToSendMessage.isEmpty()) {
            //отправляем всем кроме себя
            for (ClientManager client : clients) {
                if (!client.name.equals(name)) {
                    send(message, client);
                }
            }
        } else {
            for (ClientManager client : clients) {
                //отправляем только ему
                if (client.name.equals(clientToSendMessage)) {
                    send(message.replace("@", ""), client);
                }
            }
        }
    }

    private void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader){
        removeClient();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name +  " покинул чат.");
    }

    private String findClientNameFromMessage(String message){
        String clientName;
        String[] str = message.split(" ");
        clientName = str[1];
        if (clientName.startsWith("@")) {
            clientName = clientName.substring(1);
        } else {
            clientName = "";
        }
        return clientName;
    }

    private void send(String message, ClientManager client){
        try {
            client.bufferedWriter.write(message);
            client.bufferedWriter.newLine();
            client.bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(this.socket, bufferedWriter, bufferedReader);
        }
    }
}
