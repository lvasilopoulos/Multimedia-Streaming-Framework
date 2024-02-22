package com.distributedsystems2022.androidapp;

import android.app.Application;
import android.os.AsyncTask;
import android.widget.Switch;

import com.distributedsystems2022.consoleapp.Utilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientApplication extends Application {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String[] topics;
    private String topic, ip, username;
    private int port;
    private Boolean gotTopics = false;
    private Boolean connected = false;

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public Boolean gotTopics() {
        return gotTopics;
    }

    public String[] getTopics() {
        return topics;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void connectToServer() {
        ClientTask task = new ClientTask();
        task.execute("initialize");
    }

    public Boolean isConnected() {
        return connected;
    }

    public void connectToTopicBroker(String ip, int port) {
        if (this.ip != ip && this.port != port) {
            this.ip = ip;
            this.port = port;
            ClientTask task = new ClientTask();
            task.execute("redirect");
        }
    }

    public void closeEverything() {
        connected =false;
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class ClientTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            int numberOfTopics;
            String answer;
            String state = params[0];
            switch (state) {
                case "initialize":
                    System.out.println("Initializing");
                    try {
                        ip = BrokerInfo.getIp();
                        port = BrokerInfo.getPort();
                        if (connected) closeEverything();
                        socket = new Socket(ip, port);
                        outputStream = new ObjectOutputStream(socket.getOutputStream());
                        inputStream = new ObjectInputStream(socket.getInputStream());
                        connected = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "redirect":
                    System.out.println("Redirect called");
                    try {
                        closeEverything();
                        socket = new Socket(ip, port);
                        outputStream = new ObjectOutputStream(socket.getOutputStream());
                        inputStream = new ObjectInputStream(socket.getInputStream());
                        connected = true;
                        outputStream.writeObject(username); //Sends Username
                        outputStream.flush();
                        outputStream.writeObject("Notfirst");
                        outputStream.flush();
                        outputStream.writeObject(topic);
                        outputStream.flush();
                    } catch (IOException e) {
                        System.out.println("Redirect failed");
                        e.printStackTrace();
                    }
                    break;
                case "connect":
                    try {
                        socket = new Socket(params[1], Integer.parseInt(params[2]));
                        outputStream = new ObjectOutputStream(socket.getOutputStream());
                        inputStream = new ObjectInputStream(socket.getInputStream());
                        outputStream.writeObject(params[3]);
                        outputStream.flush();
                        outputStream.writeObject("first"); //Stelnei oti einai h prwth fora poy mpainei
                        outputStream.flush();
                        numberOfTopics = (int) inputStream.readObject();
                        topics = new String[numberOfTopics];
                        for (int i = 0; i < numberOfTopics; i++) {
                            answer = (String) inputStream.readObject();
                            topics[i] = answer;
                        }
                        gotTopics = true;
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
            }
            return null;
        }
    }
}
