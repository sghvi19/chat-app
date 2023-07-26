package com.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;

    private String chatName;

    public Client(Socket socket,String userName,String chatName){
        try {
            this.socket = socket;
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.userName = userName;
            this.chatName = chatName;
        }catch (IOException e){
            closeEverything();
        }
    }

    private void sendMsg(){
        try {
            write(userName);
            write(chatName);

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String msgToSend = scanner.nextLine();
                write(userName + ":" + msgToSend);
            }
        }catch (Exception e){
            closeEverything();
        }
    }

    private void write(String toWrite) throws IOException {
        bufferedWriter.write(toWrite);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    private void listenForMsg() {
        new Thread(() -> {
            String msgFromChat;
            while(socket.isConnected()){
                try {
                    msgFromChat = bufferedReader.readLine();
                    System.out.println(msgFromChat);
                }catch (Exception e){
                    closeEverything();
                }
            }

        }).start();
    }

    private void closeEverything() {
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

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for chat: ");
        String user = scanner.nextLine();
        System.out.println("Enter chat name you want to join: ");
        String chat = scanner.nextLine();
        Socket socket1 = new Socket("localhost",1211);
        Client client = new Client(socket1,user,chat);
        client.listenForMsg();
        client.sendMsg();
    }

}
