package com.distributedsystems2022.consoleapp;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class Client {
    boolean first = true;
    boolean correctBroker = false;
    boolean flag = true;
    private Socket socket;
    public ObjectInputStream inputStream;
    public ObjectOutputStream outputStream;
    private String username;
    private Boolean isOccupied=false;
    private String[] supportedFormats = {".mp4",".jpg",".png"};

    private int port;

    public Client(String username, String ip, int port) {
        this.socket = socket;
        this.username = username;
        System.out.println(ip + " " + port + " ");
        connectToServer(ip, port, username, "");
//        System.out.println("Created " +t.getNumber()  + " " + t.isFlag());
    }

    public void connectToServer(String ip, int port, String username, String topic) {

        String receiveMsg = "";
        String sendMsg = "";
        String newIp = "";
        int newPort = 0;
        Scanner scanner = new Scanner(System.in);


        try {
            socket = new Socket(ip, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream.writeObject(username); //Sends Username
            outputStream.flush();
            if (topic.compareTo("") == 0) {
                if (first) { //An einai h prwth fora poy loggarei
                    String answer = "first";
                    int numberOfTopics;
                    String[] topics;
                    outputStream.writeObject(answer); //Stelnei oti einai h prwth fora poy mpainei
                    outputStream.flush();
                    numberOfTopics = (int) inputStream.readObject();
                    topics = new String[numberOfTopics];
                    for (int i = 0; i < numberOfTopics; i++) {
                        answer = (String) inputStream.readObject();
                        topics[i] = answer;
                    }
                    System.out.println("What topic are you looking for? ");
                    while (!Arrays.asList(topics).contains(topic)) {
                        System.out.println(Arrays.toString(topics));
                        topic = scanner.nextLine();
                        if (!Arrays.asList(topics).contains(topic))
                            System.out.println("Topic doesn't exist. Try again");
                    }
                    System.out.println(username + " selected to join " + topic + " channel");
                    outputStream.writeObject(topic);
                    outputStream.flush();
                    newIp = (String) inputStream.readObject();
                    newPort = (int) inputStream.readObject();
                    System.out.println("ip is: " + newIp + ", port is: " + newPort);
                    first = false;

                    if (newPort != port && newIp != ip) {
                        try {
                            correctBroker = true;
                            this.setPort(newPort);
                            //System.out.println("IAM HERE");

                            inputStream.close();
                            outputStream.close();
                            socket.close();
                            connectToServer(newIp, newPort, username, topic);
                            //count++;


                        } catch (Exception exception) {
                            exception.printStackTrace();
                        } //finally {

                        //                       connectToServer(newIp, newPort, username, topic);
                        //}
                    } else {
                        correctBroker = true;
                    }
                }
                if (correctBroker && flag) {
                    flag = false;
                    System.out.println("Welcome! " + username);

                    outputStream.writeObject(topic);
                    outputStream.flush();

                    this.listenForMessage();
                    this.sendMessage();
                }
            } else {
                String answer = "Notfirst";
                outputStream.writeObject(answer);
                outputStream.flush();
                outputStream.writeObject(topic);
                outputStream.flush();

                this.listenForMessage();
                this.sendMessage();
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void closeEverything(Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage() {
        try {
            Scanner userInput = new Scanner(System.in);
            while (socket.isConnected()) {
                if (!isOccupied) {
                    String messageToSend = userInput.nextLine();
                    if (!(messageToSend.isEmpty() || messageToSend.trim().length() == 0)) {
                        if (!(messageToSend.startsWith("#"))) {
                            outputStream.writeObject(username + ": " + messageToSend);
                            outputStream.flush();
                        } else {
                            if (Files.exists(Paths.get(messageToSend.substring(1)))&& Arrays.asList(supportedFormats).contains(messageToSend.substring(messageToSend.lastIndexOf(".")).toLowerCase(Locale.ROOT))
                            ) {
                                outputStream.writeObject(messageToSend);
                                outputStream.flush();
                                try {
                                    sendFile(messageToSend.substring(1));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("System: File not Found or Format not supported");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            closeEverything(socket, inputStream, outputStream);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = (String) inputStream.readObject();
                        if (!msgFromGroupChat.startsWith("#")) {
                            System.out.println("\n" + msgFromGroupChat);
                        } else {
                            System.out.println("\n" + msgFromGroupChat.substring(1));
                            receiveFile();
                        }
                    } catch (IOException e) {
                        closeEverything(socket, inputStream, outputStream);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void sendFile(String path) {
        FileInputStream fileInputStream = null;
        int partCounter = 1;
        int sizeOfChunks = 1024 * 512;// 512KB
        byte[] chunks = new byte[sizeOfChunks];
        File myFile = null;
        try {
            isOccupied =true;
            myFile = new File(path);
            fileInputStream = new FileInputStream(myFile);
            int chunksAmount = (int) myFile.length() / sizeOfChunks;
            int lastpiece = (int) myFile.length() % sizeOfChunks;
            String fileName = myFile.getName();
            int totalChunks = chunksAmount + 1;
            outputStream.writeObject(fileName);
            outputStream.flush();
            outputStream.writeObject(totalChunks);
            outputStream.flush();
            outputStream.writeObject(sizeOfChunks);
            outputStream.flush();
            MediaFile mediaFile = null;
            byte[] temp = null;
            for (int i = 0; i < totalChunks; i++) {
                String filePartName = String.format("%03d_%s", partCounter++, fileName);
                if (i == totalChunks - 1) {
                    fileInputStream.read(chunks, 0, lastpiece);
                    temp = Arrays.copyOf(chunks, lastpiece);
                } else {
                    fileInputStream.read(chunks, 0, sizeOfChunks);
                    temp = Arrays.copyOf(chunks, sizeOfChunks);
                }
                mediaFile = new MediaFile(filePartName, username, Utilities.dateToString(), temp);
                outputStream.writeObject(mediaFile);
                outputStream.flush();
            }
            fileInputStream.close();
            System.out.println("System: file sent");
            isOccupied =false;

        } catch (IOException e) {
            System.out.println("Could not open file.");
        }
    }


    public void receiveFile() {
        FileOutputStream fileOutputStream = null;
        try {
            String filename = (String) inputStream.readObject();
            int totalChunks = Integer.parseInt((String) inputStream.readObject());
            File myFile = new File(filename);
            fileOutputStream = new FileOutputStream(myFile);

            for (int i = 0; i < totalChunks; i++) {
                //System.out.println("Receiving chunk: "+Integer.toString(i+1)+"|"+Integer.toString(totalChunks));
                MediaFile mediaFile = (MediaFile) inputStream.readObject();
                fileOutputStream.write(mediaFile.getMultimediaFileChunks());
            }
            fileOutputStream.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static void main(String args[]) throws Exception {
        String[] broker = Utilities.getRandomBroker("BrokerInfo.txt");
        Client client = new Client(args[0], broker[0], Integer.parseInt(broker[1]));
    }
}