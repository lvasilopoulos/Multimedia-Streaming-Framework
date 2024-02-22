package com.distributedsystems2022.androidapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.distributedsystems2022.androidapp.ClientApplication;
import com.distributedsystems2022.androidapp.R;
import com.distributedsystems2022.androidapp.adapters.TopicArrayAdapter;
import com.distributedsystems2022.androidapp.interfaces.RecyclerViewInterface;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class TopicSelection extends AppCompatActivity implements RecyclerViewInterface {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ArrayList<String> topics = new ArrayList<String>();
    private String topic = "";
    private String username;
    private RecyclerView recyclerView;
    private TopicArrayAdapter adapter;
    private boolean gotTopics = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_selection);
        recyclerView = findViewById(R.id.topicRecyclerView);
        adapter = new TopicArrayAdapter(this, topics, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getIntent().hasExtra("username"))
            username = getIntent().getExtras().getString("username");
        ((ClientApplication) getActivity().getApplication()).setUsername(username);
    }

    private Activity getActivity() {
        return this;
    }

    @Override
    public void onItemClick(int position) {
        topic = topics.get(position);
        ((ClientApplication) getActivity().getApplication()).setTopic(topic);
        EnterTopic task = new EnterTopic();
        task.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        topics.clear();
        adapter.updateList(topics);
        ((ClientApplication) getActivity().getApplication()).connectToServer();
        while (!((ClientApplication) getActivity().getApplication()).isConnected());
        socket = ((ClientApplication) getActivity().getApplication()).getSocket();
        inputStream = ((ClientApplication) getActivity().getApplication()).getInputStream();
        outputStream = ((ClientApplication) getActivity().getApplication()).getOutputStream();
        LoadTopics task = new LoadTopics();
        task.execute();
        while (!gotTopics) ;
        adapter.updateList(topics);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class LoadTopics extends AsyncTask<Void, Void, Void> {
        private String resp = null;
        private AlertDialog nameError;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            int numberOfTopics;
            String answer;

            try {
                outputStream.writeObject(username);
                outputStream.flush();
                outputStream.writeObject("first"); //Stelnei oti einai h prwth fora poy mpainei
                outputStream.flush();
                numberOfTopics = (int) inputStream.readObject();
                for (int i = 0; i < numberOfTopics; i++) {
                    answer = (String) inputStream.readObject();
                    topics.add(answer);
                }
                gotTopics = true;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }


    private class EnterTopic extends AsyncTask<Void, Void, Void> {
        private String resp = null;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(TopicSelection.this,
                    "Wait",
                    "Connecting to topic...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            int newPort;
            String newIp;
            try {
                outputStream.writeObject(topic);
                outputStream.flush();
                newIp = (String) inputStream.readObject();
                newPort = (int) inputStream.readObject();
                ((ClientApplication) getActivity().getApplication()).connectToTopicBroker(newIp, newPort);
                Intent enterTopic = new Intent(getApplicationContext(), TopicViewer.class);
                startActivity(enterTopic);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
        }
    }
}

