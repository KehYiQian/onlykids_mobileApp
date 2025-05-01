package com.example.onlykids_mobile_application;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.List;

public class Mistake_memo_adapter extends RecyclerView.Adapter<Mistake_memo_adapter.ViewHolder> {

    public interface OnRetryListener {
        void onRetry(Mistake_entity mistake);
    }

    private final List<Mistake_entity> mistakeList;

    private final OnRetryListener listener;

    public Mistake_memo_adapter(List<Mistake_entity> mistakeList, OnRetryListener listener) {
        this.mistakeList = mistakeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Mistake_memo_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mistake_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Mistake_memo_adapter.ViewHolder holder, int position) {
        Mistake_entity mistake = mistakeList.get(position);

        long timeUsed = mistake.timeUsedMillis / 1000;
        holder.txtQuestion.setText(mistake.question);
        holder.txtTopic.setText(MessageFormat.format("Topic: {0} (Level {1})", mistake.topic, mistake.level));
        holder.txtTime.setText(MessageFormat.format("Time used: {0}s", timeUsed));

        holder.itemView.setOnClickListener(v -> listener.onRetry(mistake));

    }

    @Override
    public int getItemCount() {
        return mistakeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtQuestion, txtTopic, txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtQuestion = itemView.findViewById(R.id.txtQuestion);
            txtTopic = itemView.findViewById(R.id.txtTopic);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}
