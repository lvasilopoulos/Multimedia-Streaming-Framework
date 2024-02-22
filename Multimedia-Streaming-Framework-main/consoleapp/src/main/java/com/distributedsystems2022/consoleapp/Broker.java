package com.distributedsystems2022.consoleapp;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Broker {
    private ServerSocket serverSocket;
    private static ConcurrentHashMap<String, ArrayList<Object>> topicHistory;
    private static ConcurrentHashMap<String, ArrayList<String>> topicUsers;
    private static String ip,brokerId,brokerHash;
    private static int port;
    private static String[][] brokers;

    public Broker(String[][] brokersInfo,String[] Topics, String ip, int port) {
        this.ip = ip;
        this.port = port;
        topicHistory = new ConcurrentHashMap<String, ArrayList<Object>>();
        topicUsers = new ConcurrentHashMap<String, ArrayList<String>>();
        this.initializeTopics(Topics);
        CreateBrokerArray(brokersInfo,Topics,ip,port);
        this.serverSocket = serverSocket;
        startServer(ip,port);
    }

    public void startServer(String ip, int port){
        Socket connection = null;
        try{
            serverSocket = new ServerSocket(port);
            while(!serverSocket.isClosed()){
              connection =  serverSocket.accept();
              System.out.println("Accepted connection : " + connection);
              ClientHandler clientHandler = new ClientHandler(connection);
              Thread thread = new Thread(clientHandler);
              thread.start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try {
                connection.close();
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void closeServerSocket(){
        try{
            if (serverSocket!=null){
                serverSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void CreateBrokerArray(String[][] brokersInfo,String[] topics, String ip, int port){
        //brokers is a 2d array
        //brokersInfo.length for all active brokers
        brokers = new String[brokersInfo.length][4];
        //gia olous tous broker pou tha dhmioyrghsw pernaw mes ton 2d pinaka brokers tou kathenos to ip,port,id,hash
        for (int i=0; i<brokersInfo.length; i++){
            brokers[i][0] = brokersInfo[i][0]; //Vazw mes ton pinaka brokers to ip twn broker
            brokers[i][1] = brokersInfo[i][1]; //port
            brokers[i][2] = MD5((brokersInfo[i][0]+brokersInfo[i][1])); //hash tou kathe broker vasei tou ip kai port toy
            brokers[i][3] = Integer.toString(i + 1); //id toy kathe broker

            //An apefthinomai ston swsto broker tote dwse to swsto hash, diaforetik pairnoun oloi toy teleutaiou
            if(brokers[i][0].equals(ip)  && Integer.parseInt(brokers[i][1]) == port) {
                brokerHash = brokers[i][2];
                brokerId = brokers[i][3];
            }
        }
        sortBrokers(brokers);
    }

    public static String MD5(String str){
        try {
            MessageDigest message = MessageDigest.getInstance("MD5");
            message.update(str.getBytes(), 0, str.length());
            BigInteger bignumber = new BigInteger(1,message.digest());
            return bignumber.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ("Can't hash");
        }
    }

    public void sortBrokers(String[][] brokers){ //sort brokers according their hash
        boolean flag = true;
        int j = 0;
        BigInteger b1;
        BigInteger b2;
        BigInteger temp;
        String tempIp = "",tempPort = "",tempBrokerId = "" ;
        while(flag){
            flag = false;
            j++;
            for(int i = 0;i < brokers.length - j;i++){
                b1 = new BigInteger(brokers[i][2]);
                b2 = new BigInteger(brokers[i + 1][2]);
                if(b1.compareTo(b2) > 0){  // b1>b2
                    tempIp = brokers[i][0];
                    tempPort = brokers[i][1];
                    temp = b1;
                    tempBrokerId = brokers[i][3];

                    brokers[i][0] = brokers[i + 1][0];
                    brokers[i][1] = brokers[i + 1][1];
                    brokers[i][2] = brokers[i + 1][2];
                    brokers[i][3] = brokers[i + 1][3];

                    brokers[i + 1][0] = tempIp;
                    brokers[i + 1][1] = tempPort;
                    brokers[i + 1][2] = temp.toString();
                    brokers[i + 1][3] = tempBrokerId;
                    flag = true;
                }
            }
        }
        for(int i=0; i<3; i++){
            System.out.println("com.distributedsystems2022.consoleApp.Broker "+brokers[i][3]+" with port:"+ brokers[i][1] +" ->"+brokers[i][2]);
        }
    }

    private void initializeTopics(String[] Topics){
        for (String topic : Topics) {
            topicHistory.put(topic, new ArrayList<Object>());
            topicUsers.put(topic, new ArrayList<String>());
        }
    }
    public static String getBrokerHash() {
        return brokerHash;
    }
    public static String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }

    public static String getBrokerIp() {
        return ip;
    }
    public static int getBrokerPort() { return port;}
    public static String[][] getBrokers(){
        return brokers;
    }

    public static ArrayList<Object> getTopicHistory(String topic){
        return topicHistory.get(topic);
    }

    public static void addUser(String username, String topic) {
        topicUsers.get(topic).add(username);
    }

    public static synchronized int getNumberOfTopics(){
        return topicUsers.size();
    };

    public static String[] getTopics(){
        return topicHistory.keySet().toArray(new String[0]);
    }

    public static boolean userInTopic(String username, String topic) {
        return topicUsers.get(topic).contains(username);
    }

    public static void removeUser(String username, String topic) {
        topicUsers.get(topic).remove(username);
    }
    public static synchronized void addLog(String topic, Object log){
        topicHistory.get(topic).add(log);
    }

    public static synchronized void addChunksToLog(String topic, String clientUsername,String filename, ArrayList<MediaFile> chunks){
        topicHistory.get(topic).add("#"+clientUsername+": sent "+ filename);
        topicHistory.get(topic).add(filename);
        topicHistory.get(topic).add(Integer.toString(chunks.size()));
        for(MediaFile chunk: chunks){
            topicHistory.get(topic).add(chunk);
        }
    }

    public static int numberOfLogs(String topic){
        return topicHistory.get(topic).size();
    }
    public static void main(String[] args) throws IOException {
        new Broker(Utilities.getBrokersInfo("BrokerInfo.txt"), Utilities.getTopics("Topics.txt"), args[0], Integer.parseInt(args[1]));
    }
}