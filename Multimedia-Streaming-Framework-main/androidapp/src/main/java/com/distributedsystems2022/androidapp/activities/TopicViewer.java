package com.distributedsystems2022.androidapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.PathUtils;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.distributedsystems2022.androidapp.AndroidUtilities;
import com.distributedsystems2022.androidapp.AsyncLoader;
import com.distributedsystems2022.androidapp.ClientApplication;
import com.distributedsystems2022.androidapp.Message;
import com.distributedsystems2022.androidapp.R;
import com.distributedsystems2022.androidapp.adapters.MessageArrayAdapter;
import com.distributedsystems2022.androidapp.interfaces.RecyclerViewInterface;
import com.distributedsystems2022.consoleapp.MediaFile;
import com.distributedsystems2022.consoleapp.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TopicViewer extends AppCompatActivity implements RecyclerViewInterface {
    public static final int PICK_IMAGE = 1;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String username, topic;
    private RecyclerView messageRecycler;
    private MessageArrayAdapter adapter;
    private TextView topicField;
    private EditText messageField;
    private ImageButton sendMediaButton;
    private ImageButton sendMessageButton;

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        new SendFile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri.getPath());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_interface);
        topicField = findViewById(R.id.topic_name);
        messageField = findViewById(R.id.message_field);
        sendMediaButton = findViewById(R.id.send_media_button);
        sendMessageButton = findViewById(R.id.send_message_button);
        messageRecycler = findViewById(R.id.message_recycler);
        adapter = new MessageArrayAdapter(this, new ArrayList<Message>(), this);
        messageRecycler.setLayoutManager(new LinearLayoutManager(this));
        messageRecycler.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        topic = ((ClientApplication) getActivity().getApplication()).getTopic();
        username = ((ClientApplication) getActivity().getApplication()).getUsername();
        while (!((ClientApplication) getActivity().getApplication()).isConnected()) ;
        socket = ((ClientApplication) getActivity().getApplication()).getSocket();
        inputStream = ((ClientApplication) getActivity().getApplication()).getInputStream();
        outputStream = ((ClientApplication) getActivity().getApplication()).getOutputStream();
        topicField.setText(topic);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!messageField.getText().toString().equals("")) {
                    SendMessage task = new SendMessage();
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, messageField.getText().toString());
                    messageField.setText("");
                }
            }
        });
        sendMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ChooseFile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        new ListenForMessages().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Activity getActivity() {
        return this;
    }

    @Override
    public void onItemClick(int position) {
        String path = adapter.getPath(position);
        String filename = adapter.getFilename(position);
        //new PlayVideo().execute(path, filename);
        Intent playVideo = new Intent(getApplicationContext(), VideoPlayer.class);
            playVideo.putExtra("path", path);
            playVideo.putExtra("filename", filename);
            startActivity(playVideo);
    }

    private class ChooseFile extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... args) {
            Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            data.addCategory(Intent.CATEGORY_OPENABLE);
            data.setType("*/*");
            String[] mimeTypes = {"image/*", "video/*"};
            data.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            data = Intent.createChooser(data, "Choose a file");
            activityResultLauncher.launch(data);
            return null;
        }
    }


//    private class PlayVideo extends AsyncTask<String, Void, Void> {
//        @Override
//        protected Void doInBackground(String... args) {
//            Intent playVideo = new Intent(getApplicationContext(), VideoPlayer.class);
//            playVideo.putExtra("path", args[0]);
//            playVideo.putExtra("filename", args[1]);
//            startActivity(playVideo);
//            return null;
//        }
//    }

//    private class ImageLoadingTask extends AsyncLoader<Void> {
//
//        public ImageLoadingTask(Context context) {
//            super(context);
//        }
//
//        @Override
//        public Void loadInBackground() {
//            String msgFromGroupChat;
//            Message message;
//            while (socket.isConnected()) {
//                try {
//                    msgFromGroupChat = (String) inputStream.readObject();
//                    if (msgFromGroupChat != null && msgFromGroupChat.length() > 1) {
//                        if (!msgFromGroupChat.startsWith("#")) {
//                            System.out.println(msgFromGroupChat);
//                            message = new Message(username, msgFromGroupChat);
//                            try {
//                                Message finalMessage = message;
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        adapter.addMessage(finalMessage);
//                                        messageRecycler.scrollToPosition(adapter.getBottom());
//                                    }
//                                });
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            receiveFile(msgFromGroupChat.substring(1));
//                        }
//                    }
//                } catch (IOException e) {
//                    try {
//                        System.out.println("Got IO exception at listen for messages");
//                        Thread.sleep(5000);
//                    } catch (InterruptedException ex) {
//                        ex.printStackTrace();
//                    }
//                    ((ClientApplication) getActivity().getApplication()).closeEverything();
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//            return null;
//        }
//    }

    private class ListenForMessages extends AsyncTask<Void, Void, Void> {
        @Override
        public Void doInBackground(Void... params) {
            String msgFromGroupChat;
            Message message;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = (String) inputStream.readObject();
                    if (msgFromGroupChat != null && msgFromGroupChat.length() > 1) {
                        if (!msgFromGroupChat.startsWith("#")) {
                            System.out.println(msgFromGroupChat);
                            message = new Message(username, msgFromGroupChat);
                            try {
                                Message finalMessage = message;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.addMessage(finalMessage);
                                        messageRecycler.scrollToPosition(adapter.getBottom());
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            receiveFile(msgFromGroupChat.substring(1));
                        }
                    }
                } catch (IOException e) {
                    try {
                        System.out.println("Got IO exception at listen for messages");
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    ((ClientApplication) getActivity().getApplication()).closeEverything();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public void receiveFile(String message_str) {
        FileOutputStream fileOutputStream = null;
        Message message;
        try {
            String filename = (String) inputStream.readObject();
            int totalChunks = Integer.parseInt((String) inputStream.readObject());
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + filename);
            //File need to be downloaded
            if (!file.exists()) {
            fileOutputStream = new FileOutputStream(file);
            for (int i = 0; i < totalChunks; i++) {
                System.out.println("Receiving chunk: " + Integer.toString(i + 1) + "|" + Integer.toString(totalChunks));
                MediaFile mediaFile = (MediaFile) inputStream.readObject();
                for (byte b : mediaFile.getMultimediaFileChunks()) {
                    fileOutputStream.write(b);
                }
            }
            fileOutputStream.close();
            message = new Message(username, message_str.substring(0, message_str.indexOf(":")), file.getAbsolutePath());
            try {
                Message finalMessage = message;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addMessage(finalMessage);
                        messageRecycler.scrollToPosition(adapter.getBottom());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
            //File in storage already
            else {
                message = new Message(username, message_str.substring(0, message_str.indexOf(":")), file.getPath());
                try {
                    Message finalMessage = message;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.addMessage(finalMessage);
                            messageRecycler.scrollToPosition(adapter.getBottom());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < totalChunks; i++) {
                    MediaFile mediaFile = (MediaFile) inputStream.readObject();
                    System.out.println("Skipping chunk: " + Integer.toString(i + 1) + "|" + Integer.toString(totalChunks));
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found exception ffs");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO exception ffs");
            e.printStackTrace();
        }
    }

    private class SendFile extends AsyncTask<String, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(TopicViewer.this,
                    "Wait",
                    "Uploading File");
        }

        @Override
        protected Void doInBackground(String... strings) {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + strings[0].substring(strings[0].lastIndexOf("/") + 1);
            String filename = path.substring(path.lastIndexOf("/") + 1);
            System.out.println("Got from uri: " + path);
            FileInputStream fileInputStream = null;
            int partCounter = 1;
            int sizeOfChunks = 1024 * 512;// 512KB
            byte[] chunks = new byte[sizeOfChunks];
            try {
                File file = new File(path);
                fileInputStream = new FileInputStream(file);
                int chunksAmount = (int) file.length() / sizeOfChunks;
                int lastpiece = (int) file.length() % sizeOfChunks;
                int totalChunks = chunksAmount + 1;
                outputStream.writeObject("#" + path);
                outputStream.flush();
                System.out.println("Sent initiator");
                outputStream.writeObject(filename);
                outputStream.flush();
                System.out.println("Sent filename");
                outputStream.writeObject(totalChunks);
                outputStream.flush();
                System.out.println("Sent chunks amount");
                outputStream.writeObject(sizeOfChunks);
                outputStream.flush();
                System.out.println("Sent size of chunks");
                MediaFile mediaFile = null;
                byte[] temp = null;
                for (int i = 0; i < totalChunks; i++) {
                    System.out.println("Got in " + Integer.toString(i + 1));
                    String filePartName = String.format("%03d_%s", partCounter++, filename);
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
                    System.out.println("Sent chunk: " + Integer.toString(i + 1) + "|" + Integer.toString(totalChunks));
                }
                fileInputStream.close();
                System.out.println("System: file sent");
                Message message = new Message(username, username, file.getAbsolutePath());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addMessage(message);
                        messageRecycler.scrollToPosition(adapter.getBottom());
                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
        }
    }

    private class SendMessage extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... message) {
            Message messageToSend = new Message(username, username + ": " + message[0]);
            try {
                if (socket.isConnected()) {
                    outputStream.writeObject(messageToSend.toString());
                    outputStream.flush();
                }
                try {
                    Message finalMessage = messageToSend;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.addMessage(finalMessage);
                            messageRecycler.scrollToPosition(adapter.getBottom());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}