package com.distributedsystems2022.androidapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.distributedsystems2022.androidapp.ClientApplication;
import com.distributedsystems2022.androidapp.R;

public class VideoPlayer extends AppCompatActivity {
    private TextView title_holder;
    private VideoView videoView;
    private String path,filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        title_holder = findViewById(R.id.title_holder);
        videoView = findViewById(R.id.video_player);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().hasExtra("path"))
            path = getIntent().getExtras().getString("path");
        if (getIntent().hasExtra("filename"))
            filename = getIntent().getExtras().getString("filename");
        title_holder.setText(filename);
        videoView.setVideoURI(Uri.parse(path));
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.start();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}