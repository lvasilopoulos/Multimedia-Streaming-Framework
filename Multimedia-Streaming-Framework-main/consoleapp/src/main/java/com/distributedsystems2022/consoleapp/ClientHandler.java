package com.distributedsystems2022.consoleapp;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String clientUsername;
    private String topic;
    boolean first = true;

    public String getClientUsername() {
        return clientUsername;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public ClientHandler(Socket socket) {
        String receiveMsg = "";
        String sendMsg = "";
        this.socket = socket;

    }

    private void closeEverything(Socket socket, ObjectInputStream bufferedReader, ObjectOutputStream bufferedWriter) {
        removeClientHandler();
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

    public void broadcastMessage(String messageToSend) {
        if (!(messageToSend.startsWith("SERVER:") || messageToSend.startsWith("SERVER:")))
            Broker.addLog(topic, messageToSend);
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.getClientUsername().equals(this.getClientUsername()) && Broker.userInTopic(clientHandler.getClientUsername(), this.getTopic())) {
                    clientHandler.getOutputStream().writeObject(messageToSend);
                    clientHandler.getOutputStream().flush();
                }
            } catch (IOException e) {
                closeEverything(socket, inputStream, outputStream);
            }
        }
    }

    private String getTopic() {
        return topic;
    }

    public void broadcastChunks(String filename, ArrayList<MediaFile> chunks) {
        Broker.addChunksToLog(topic, clientUsername, filename, chunks);
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.getClientUsername().equals(this.getClientUsername()) && Broker.userInTopic(clientHandler.getClientUsername(), this.getTopic())) {
                    clientHandler.getOutputStream().writeObject("#" + clientUsername + ": sent " + filename);
                    clientHandler.getOutputStream().flush();
                    clientHandler.getOutputStream().writeObject(filename);
                    clientHandler.getOutputStream().flush();
                    clientHandler.getOutputStream().writeObject(Integer.toString(chunks.size()));
                    clientHandler.getOutputStream().flush();
                    for (MediaFile chunk : chunks) {
                        clientHandler.getOutputStream().writeObject(chunk);
                        clientHandler.getOutputStream().flush();
                    }
                }
            } catch (IOException e) {
                closeEverything(socket, inputStream, outputStream);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        Broker.removeUser(clientUsername, topic);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    public ArrayList<MediaFile> receiveFile() {
        ArrayList<MediaFile> mediaFiles = new ArrayList<MediaFile>();
        try {
            int totalChunks = (int) inputStream.readObject();
            int sizeOfChunks = (int) inputStream.readObject();

            for (int i = 0; i < totalChunks; i++) {
                MediaFile mediaFile = (MediaFile) inputStream.readObject();
                mediaFiles.add(mediaFile);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaFiles;
    }

    public void findCorrectBroker(String topicHash, int numberOfBrokers) {
        String ip = "";
        int port = 0;
        try {
            BigInteger number = new BigInteger(topicHash);
            BigInteger NumberOfBrokers = new BigInteger(String.valueOf(3));
            System.out.println("----------> " + number.mod(NumberOfBrokers));
            if (number.mod(NumberOfBrokers).compareTo(BigInteger.valueOf(0)) == 0) {
                System.out.println("Giving the FIRST broker ");
                ip = Broker.getBrokers()[0][0];
                outputStream.writeObject(ip);
                port = Integer.parseInt(Broker.getBrokers()[0][1]);
                outputStream.writeObject(port);
                System.out.println("ip is: " + ip + ", port is: " + port);
            } else if (number.mod(NumberOfBrokers).compareTo(BigInteger.valueOf(2)) == 0) {
                System.out.println("Giving the THIRD broker ");
                ip = Broker.getBrokers()[2][0];
                outputStream.writeObject(ip);
                port = Integer.parseInt(Broker.getBrokers()[2][1]);
                outputStream.writeObject(port);
                System.out.println("ip is: " + ip + ", port is: " + port);
            } else {
                System.out.println("Giving the SECOND broker ");
                ip = Broker.getBrokers()[1][0];
                outputStream.writeObject(ip);
                port = Integer.parseInt(Broker.getBrokers()[1][1]);
                outputStream.writeObject(port);
                System.out.println("ip is: " + ip + ", port is: " + port);
            }
//            else{
//                out.writeObject(this.com.distributedsystems2022.consoleApp.Broker.getBrokerIp());
//                out.writeObject(this.com.distributedsystems2022.consoleApp.Broker.getBrokerPort());
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHistory() {
        try {
//            outputStream.writeObject(com.distributedsystems2022.consoleApp.Broker.numberOfLogs(topic));
//            outputStream.flush();
            for (Object obj : Broker.getTopicHistory(topic)) {
                outputStream.writeObject(obj);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String filename;
        String messageFromClient;
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            closeEverything(socket, inputStream, outputStream);
            e.printStackTrace();
        }
        try {
            clientUsername = (String) inputStream.readObject();
            System.out.println("User " + clientUsername + " connected");
            String answer = (String) inputStream.readObject();
            if (answer.compareTo("first") == 0 && first) { //An sundeetai gia prwth fora
                System.out.println("Welcome from Broker: " + Broker.getBrokerId());
                outputStream.writeObject(Broker.getNumberOfTopics());
                outputStream.flush();
                for (String i : Broker.getTopics()) {
                    outputStream.writeObject(i);
                    outputStream.flush();
                }
                if(socket.isClosed())
                    closeEverything(socket, inputStream, outputStream);
                topic = (String) inputStream.readObject();
                System.out.println("I got topic: " + topic + " from user: " + clientUsername);
                String TopicHash = Broker.MD5(topic);
                System.out.println(TopicHash);
                //cheking if user talks to the right Broker
                first = false;
                findCorrectBroker(TopicHash, 3);
            }
            if (!first || answer.compareTo("Notfirst") == 0) {

                try {
//                   new ActionsForBrokers(connection,br);
                    System.out.println("Welcome from Broker: " + Broker.getBrokerId());
                    topic = (String) inputStream.readObject();

                    System.out.println("I got topic: " + topic + " from user: " + clientUsername);
                    Broker.addUser(clientUsername, topic);
                    this.sendHistory();
                } catch (EOFException e) {
                    closeEverything(socket, inputStream, outputStream);
                } catch (IOException e) {
                    closeEverything(socket, inputStream, outputStream);
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    closeEverything(socket, inputStream, outputStream);
                    e.printStackTrace();
                }
            }
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
        } catch (IOException e) {
            closeEverything(socket, inputStream, outputStream);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            closeEverything(socket, inputStream, outputStream);
        } catch (NullPointerException e) {
            closeEverything(socket, inputStream, outputStream);
        }
        while (socket.isConnected()) {
            try {
                messageFromClient = (String) inputStream.readObject();
                System.out.println(messageFromClient);
                if (!messageFromClient.startsWith("#")) {
                    broadcastMessage(messageFromClient);
                } else {
                    messageFromClient = messageFromClient.substring(1);
                    filename = (String) inputStream.readObject();;
                    System.out.print(filename);
                    ArrayList<MediaFile> mediaFiles = receiveFile();
                    broadcastChunks(filename, mediaFiles);
                }
            } catch (Exception e) {
                closeEverything(socket, inputStream, outputStream);
                break;
            }
        }
    }
}
