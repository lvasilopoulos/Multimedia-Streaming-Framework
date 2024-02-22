package com.distributedsystems2022.androidapp;

import android.net.Uri;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class Message {
    private String sender, body=null, type;
    private Boolean isPersonal = false;
    private String path;
    //private Path file
    public Message(String receiver, String message){
        sender = message.substring(0,message.indexOf(":"));
        body = message.substring(message.indexOf(":")+2);
        type = "text";
        if (sender.equals(receiver)) isPersonal=true;
        if (sender.equals("SERVER")) type = "server";
    }

    public Message(String receiver, String sender, String path){
        this.sender = sender;
        String[] videoExtensions = {"mp4"};
        String[] imageExtensions = {"png", "jpg"};
        String extension = path.substring(path.lastIndexOf(".")+1);
        this.path = path;
        if (new ArrayList<String>(Arrays.asList(videoExtensions)).contains(extension)){
            type="video";
        }else if(new ArrayList<String>(Arrays.asList(imageExtensions)).contains(extension)){
            type="image";
        }
        if (sender.equals(receiver)) isPersonal=true;
    }

    public String getFilename(){
        return path.substring(path.lastIndexOf("/")+1,path.lastIndexOf("."));
    }

    public String getPath() {
        return path;
    }

    public Uri getUri() {
        return Uri.parse(path);
    }


    public boolean isOwnMessage(){
        return isPersonal;
    }

    public boolean isServerUpdate(){
        if (type.equals("server")){
            return true;
        }
        return false;
    }

    public String getSender() {
        return sender;
    }

    public String toString(){
        return sender+": "+body;
    }

    public String getBody() {
        return body;
    }

    public String getType() {
        return type;
    }
}
