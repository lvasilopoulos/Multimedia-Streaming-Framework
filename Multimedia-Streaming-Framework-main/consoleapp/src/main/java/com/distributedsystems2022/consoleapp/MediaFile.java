package com.distributedsystems2022.consoleapp;

import java.io.Serializable;

public class MediaFile implements Serializable {
    private String fileName, profileName, dateCreated;//length,frameRate,frameWidth,frameHeight
    private byte[] multimediaFileChunks;

    public MediaFile(String fileName, String profileName, String dateCreated,  byte[] multimediaFileChunk) { //String length, String frameRate, String frameWidth, String frameHeight
        this.fileName = fileName;
        this.profileName = profileName;
        this.dateCreated = dateCreated;
//        this.length = length;
//        this.frameRate = frameRate;
//        this.frameWidth = frameWidth;
//        this.frameHeight = frameHeight;
        this.multimediaFileChunks = multimediaFileChunk;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public byte[] getMultimediaFileChunks() {
        return multimediaFileChunks;
    }

    public void setMultimediaFileChunk(byte[] multimediaFileChunk) {
        this.multimediaFileChunks = multimediaFileChunk;
    }
}
