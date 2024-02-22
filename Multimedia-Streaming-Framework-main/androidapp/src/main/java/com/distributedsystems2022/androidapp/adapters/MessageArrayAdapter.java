package com.distributedsystems2022.androidapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.distributedsystems2022.androidapp.Message;
import com.distributedsystems2022.androidapp.R;
import com.distributedsystems2022.androidapp.interfaces.RecyclerViewInterface;

import java.util.ArrayList;

public class MessageArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TEXT_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_TEXT_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_VIDEO_MESSAGE_SENT = 3;
    private static final int VIEW_TYPE_VIDEO_MESSAGE_RECEIVED = 4;
    private static final int VIEW_TYPE_IMAGE_MESSAGE_SENT = 5;
    private static final int VIEW_TYPE_IMAGE_MESSAGE_RECEIVED = 6;
    private static final int VIEW_TYPE_SERVER_MESSAGE = 7;

    private Context context;
    private ArrayList<Message> messages;
    private final RecyclerViewInterface recyclerViewInterface;

    public MessageArrayAdapter(Context context, ArrayList<Message> messages, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.messages = messages;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @Override
    public int getItemCount() {
        if (messages != null)
            return messages.size();
        else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        if (message.isServerUpdate()) {
            return VIEW_TYPE_SERVER_MESSAGE;
        }
        if (message.getType().equals("text")) {
            if (message.isOwnMessage()) {
                return VIEW_TYPE_TEXT_MESSAGE_SENT;
            } else {
                return VIEW_TYPE_TEXT_MESSAGE_RECEIVED;
            }
        } else if (message.getType().equals("video")) {
            if (message.isOwnMessage()) {
                return VIEW_TYPE_VIDEO_MESSAGE_SENT;
            } else {
                return VIEW_TYPE_VIDEO_MESSAGE_RECEIVED;
            }
        } else {
            if (message.isOwnMessage()) {
                return VIEW_TYPE_IMAGE_MESSAGE_SENT;
            } else {
                return VIEW_TYPE_IMAGE_MESSAGE_RECEIVED;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_TEXT_MESSAGE_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_text_send, parent, false);
                return new SentTextMessageHolder(view);
            case VIEW_TYPE_TEXT_MESSAGE_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_text_receive, parent, false);
                return new ReceivedTextMessageHolder(view);
            case VIEW_TYPE_SERVER_MESSAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_server_update, parent, false);
                return new ServerMessageHolder(view);
            case VIEW_TYPE_VIDEO_MESSAGE_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_video_receive, parent, false);
                return new ReceivedVideoMessageHolder(view, recyclerViewInterface);
            case VIEW_TYPE_IMAGE_MESSAGE_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_image_receive, parent, false);
                return new ReceivedImageMessageHolder(view);
            case VIEW_TYPE_VIDEO_MESSAGE_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_video_send, parent, false);
                return new SentVideoMessageHolder(view, recyclerViewInterface);
            case VIEW_TYPE_IMAGE_MESSAGE_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_image_send, parent, false);
                return new SentImageMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message current = messages.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_TEXT_MESSAGE_SENT:
                ((SentTextMessageHolder) holder).bind(current);
                break;
            case VIEW_TYPE_TEXT_MESSAGE_RECEIVED:
                ((ReceivedTextMessageHolder) holder).bind(current);
                break;
            case VIEW_TYPE_SERVER_MESSAGE:
                ((ServerMessageHolder) holder).bind(current);
                break;
            case VIEW_TYPE_VIDEO_MESSAGE_RECEIVED:
                ((ReceivedVideoMessageHolder) holder).bind(current);
                break;
            case VIEW_TYPE_IMAGE_MESSAGE_RECEIVED:
                ((ReceivedImageMessageHolder) holder).bind(current);
                break;
            case VIEW_TYPE_VIDEO_MESSAGE_SENT:
                ((SentVideoMessageHolder) holder).bind(current);
                break;
            case VIEW_TYPE_IMAGE_MESSAGE_SENT:
                ((SentImageMessageHolder) holder).bind(current);
                break;
        }
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public int getBottom() {
        if (messages.size() > 1)
            return messages.size() - 1;
        else
            return 0;
    }

    public String getPath(int position) {
        return messages.get(position).getPath();
    }

    public String getFilename(int position) {
        return messages.get(position).getFilename();
    }

    private class SentTextMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageBodyField;

        public SentTextMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageBodyField = itemView.findViewById(R.id.message_body);
        }

        void bind(Message message) {
            messageBodyField.setText(message.getBody());
        }
    }

    private class ReceivedTextMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageSenderField;
        private TextView messageBodyField;

        public ReceivedTextMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageSenderField = itemView.findViewById(R.id.message_sender);
            messageBodyField = itemView.findViewById(R.id.message_body);
        }

        void bind(Message message) {
            messageSenderField.setText(message.getSender());
            messageBodyField.setText(message.getBody());
        }
    }

    public static class ReceivedVideoMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageSenderField, videoTitle;
        private ImageButton playVideoButton;
        private String path;

        public ReceivedVideoMessageHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            messageSenderField = itemView.findViewById(R.id.message_sender);
            videoTitle = itemView.findViewById(R.id.video_title);
            playVideoButton = itemView.findViewById(R.id.play_video_button);

            playVideoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewInterface != null) {
                        int position = getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }

        void bind(Message message) {
            path = message.getPath();
            messageSenderField.setText(message.getSender());
            videoTitle.setText(message.getFilename());
        }

        public String getPath() {
            return path;
        }
    }

    public static class SentVideoMessageHolder extends RecyclerView.ViewHolder {
        private TextView videoTitle;
        private ImageButton playVideoButton;
        private String path;

        public SentVideoMessageHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            videoTitle = itemView.findViewById(R.id.video_title);
            playVideoButton = itemView.findViewById(R.id.play_video_button);

            playVideoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewInterface != null) {
                        int position = getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }

        void bind(Message message) {
            path = message.getPath();
            videoTitle.setText(message.getFilename());
        }

        public String getPath() {
            return path;
        }

    }

    private class ReceivedImageMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageSenderField;
        private ImageView imageView;

        public ReceivedImageMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageSenderField = itemView.findViewById(R.id.message_sender);
            imageView = itemView.findViewById(R.id.image_holder);
        }

        void bind(Message message) {
            messageSenderField.setText(message.getSender());
            System.out.println("URI in message: "+message.getUri().toString());
            imageView.setImageURI(message.getUri());
        }
    }

    private class SentImageMessageHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public SentImageMessageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_holder);
        }

        void bind(Message message) {
            System.out.println("URI in message: "+message.getUri().toString());
            imageView.setImageURI(message.getUri());
        }
    }

    private class ServerMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageBodyField;

        public ServerMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageBodyField = itemView.findViewById(R.id.message_body);
        }

        void bind(Message message) {
            messageBodyField.setText(message.getBody());
        }
    }
}

