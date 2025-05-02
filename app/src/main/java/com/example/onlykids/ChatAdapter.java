package com.example.onlykids;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.messageTextView.setText(chatMessage.getMessage());

        // Adjust the gravity of the parent LinearLayout based on message type
        if (chatMessage.isUserMessage()) {
            holder.container.setGravity(android.view.Gravity.END);
            holder.messageTextView.setBackgroundResource(R.drawable.user_message_bubble);
        } else {
            holder.container.setGravity(android.view.Gravity.START);
            holder.messageTextView.setBackgroundResource(R.drawable.assistant_message_bubble);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        LinearLayout container;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            container = (LinearLayout) itemView; // The root LinearLayout
        }
    }
}