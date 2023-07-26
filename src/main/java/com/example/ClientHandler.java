package com.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

public class ClientHandler implements Runnable{
    private static HashMap<String,List<ClientHandler>> chatClients = new HashMap<>();

    private static HashMap<String,List<String>> chatHistory = new HashMap<>();
    public Socket socket;
    public BufferedReader bufferedReader;
    public BufferedWriter bufferedWriter;
    public String clientName;

    public String chatName;


    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientName = bufferedReader.readLine();
            this.chatName = bufferedReader.readLine();
            addClientToChat();
            broadcastMessage("SERVER:" + clientName + " has entered the chat!");
        }catch (IOException e){
            closeEverything();
        }
    }

    private void addClientToChat(){
        if(chatClients.containsKey(chatName)){
            chatClients.get(chatName).add(this);
            if(chatHistory.get(chatName) != null){
                writeOldMessages();
            }
        }else{
            List<ClientHandler> clientHandlers = new ArrayList<>();
            clientHandlers.add(this);
            chatClients.put(chatName,clientHandlers);
        }
    }

    private void writeOldMessages() {
        chatHistory.get(chatName).stream().forEach(line -> {
            try {
                write(this,line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void run() {
        String messageFromClient;

        while(socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);

            }catch (IOException e){
                closeEverything();
                break;
            }
        }
    }

    private void removeClientHandler(){
        chatClients.get(chatName).remove(this);
        broadcastMessage("SERVER:" + clientName + " has left the chat!");
    }

    private void broadcastMessage(String messageToSend) {
        writeToChatHistory(messageToSend);

        chatClients.get(chatName).stream().forEach(clientHandler -> {
            try {
                write(clientHandler, messageToSend);
            }catch(IOException e) {
                closeEverything();
            }
        });
    }

    private void writeToChatHistory(String messageToSend) {
        if(chatHistory.containsKey(chatName)){
            chatHistory.get(chatName).add(messageToSend);
        }else{
            List<String> msgs = new ArrayList<>();
            msgs.add(messageToSend);
            chatHistory.put(chatName,msgs);
        }
    }

    private void write(ClientHandler clientHandler,String message) throws IOException {
            clientHandler.bufferedWriter.write(message);
            clientHandler.bufferedWriter.newLine();
            clientHandler.bufferedWriter.flush();
        }


    private void closeEverything() {
        removeClientHandler();
        try {
            if(socket != null){
                socket.close();
            }

            if(bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }


        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
