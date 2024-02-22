package com.distributedsystems2022.consoleapp;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class Utilities {

//    public static com.distributedsystems2022.consoleApp.MediaFile mediaFileToInstance(String path){
//
//    }

    public static String getFilename(String path){
        return path.split("\\\\")[path.split("\\\\").length-1];
    }

    public static String dateToString(){
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        return currentDateTime.format(formatter);
    }

    public static LocalDateTime stringToDate(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return LocalDateTime.parse(date, formatter);
    }

    public static String[] getTopics(String str){
        String[] topics =null;
        String line;
        File f= null;
        try{
            f = new File(str);
        }
        catch(NullPointerException e){
            System.err.println("The file was not found.");
        }
        try{
            BufferedReader reader = new BufferedReader(new FileReader(f));
            Path path = Paths.get(str);
            long lineCount = Files.lines(path).count();

            topics = new String[(int)lineCount];
            line= reader.readLine();
            int count=0;
            while (line!=null){
                String temp = line;
                topics[count] = temp;
                count++;
                line= reader.readLine();
            }
            return  topics;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public static String[][] getBrokersInfo(String str){
        String[][] brokersInfo;
        String line;
        File f= null;
        try{
            f = new File(str);
        }
        catch(NullPointerException e){
            System.err.println("The file was not found.");
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            Path path = Paths.get(str);
            long lineCount = Files.lines(path).count();

            brokersInfo  = new String[(int)lineCount][2];
            line = reader.readLine();
            int count =0;
            while(line!=null){
                String[] temp =  line.split(" ");
                brokersInfo[count][0] = temp[0]; //ip
                brokersInfo[count][1] = temp[1]; //port
                count++;
                line = reader.readLine();
            }
            return  brokersInfo;
        }catch(IOException e){
            System.out.println("Could not open file.");
        }
        return null;
    }

    public static String[] getRandomBroker(String file) {
        int rng = ThreadLocalRandom.current().nextInt(0, 3);
        String[][] brokers = getBrokersInfo(file);
        return new String[]{brokers[rng][0],brokers[rng][1]};
    }
}
