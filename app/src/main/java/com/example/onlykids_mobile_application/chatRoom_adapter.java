package com.example.onlykids_mobile_application;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class chatRoom_adapter extends RecyclerView.Adapter<chatRoom_adapter.ChatViewHolder> {

    private final List<chatRoom_model> chatMessages;

    //constructor
    public chatRoom_adapter(List<chatRoom_model> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_bubble, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        chatRoom_model chat = chatMessages.get(position);
        holder.chatText.setText(chat.getMessage());

        LinearLayout layout = holder.chatBubbleLayout;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.chatText.getLayoutParams();

        if (chat.getSender().equals("user")) {
            // user at right
            holder.botIcon.setVisibility(View.GONE);
            holder.chatText.setBackgroundResource(R.drawable.bg_bubble);
            layout.setGravity(Gravity.END);
        } else {
            // bot at left
            holder.botIcon.setVisibility(View.VISIBLE);
            holder.chatText.setBackgroundResource(R.drawable.bg_bubble);
            layout.setGravity(Gravity.START);
        }

        holder.chatText.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatText;
        ImageView botIcon;
        LinearLayout chatBubbleLayout;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatText = itemView.findViewById(R.id.chatText);
            botIcon = itemView.findViewById(R.id.botIcon);
            chatBubbleLayout = itemView.findViewById(R.id.chatBubbleLayout);
        }
    }



}