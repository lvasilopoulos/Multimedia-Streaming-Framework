package com.distributedsystems2022.androidapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.distributedsystems2022.androidapp.R;

public class MainActivity extends AppCompatActivity {
    private ImageButton usernameButton;
    AlertDialog nameError;
    private EditText usernameField;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameButton = (ImageButton) findViewById(R.id.username_button);
        nameError = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog).create();
    }



    public void onStart() {
        super.onStart();
        usernameButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                usernameField = (EditText) findViewById(R.id.username_field);
                if (usernameField.getText().toString().equals("")) {
                    nameError.setTitle("Hint");
                    nameError.setMessage("Empty username");
                    nameError.setButton(AlertDialog.BUTTON_NEUTRAL, "Close hint", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int k) {
                            dialog.dismiss();
                        }
                    });
                    nameError.show();
                } else {
                    ClientTask task = new ClientTask();
                    task.execute(usernameField.getText().toString());
                }
            }
        });
    }






/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class ClientTask extends AsyncTask<String, String, String> {
        private String resp=null;
        ProgressDialog progressDialog;
        @Override
        protected String doInBackground(String... params) {
            Intent selectTopic = new Intent(getApplicationContext(), TopicSelection.class);
            selectTopic.putExtra("username",params[0]);
            startActivity(selectTopic);
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Wait",
                    "Connecting to server...");
        }
    }
}


