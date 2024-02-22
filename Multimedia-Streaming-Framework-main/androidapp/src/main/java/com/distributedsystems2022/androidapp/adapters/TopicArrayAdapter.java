package com.distributedsystems2022.androidapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.distributedsystems2022.androidapp.R;
import com.distributedsystems2022.androidapp.interfaces.RecyclerViewInterface;

import java.util.ArrayList;

public class TopicArrayAdapter extends RecyclerView.Adapter<TopicArrayAdapter.TopicViewHolder> {
    private Context context;
    private ArrayList<String> dataset;
    private final RecyclerViewInterface recyclerViewInterface;

    public  TopicArrayAdapter(Context context, ArrayList<String> dataset, RecyclerViewInterface recyclerViewInterface){
        this.context=context;
        this.dataset=dataset;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.topic_item,parent,false);
        return new TopicViewHolder(view, recyclerViewInterface);
    }

    public void updateList(ArrayList<String> dataset){
        this.dataset = dataset;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        holder.button.setText(dataset.get(position));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public static class TopicViewHolder extends  RecyclerView.ViewHolder{
        Button button;
        public TopicViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            button = itemView.findViewById(R.id.topicButton);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(recyclerViewInterface!=null){
                        int position = getAbsoluteAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}

