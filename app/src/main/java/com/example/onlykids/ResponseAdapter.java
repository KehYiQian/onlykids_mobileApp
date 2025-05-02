package com.example.onlykids;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ResponseAdapter extends RecyclerView.Adapter<ResponseAdapter.ResponseViewHolder> {
    private List<String> responses;
    private List<Boolean> selectedStates; // Tracks which responses are selected

    public ResponseAdapter(List<String> responses) {
        this.responses = responses;
        this.selectedStates = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            selectedStates.add(false); // Initially, no responses are selected
        }
    }

    @NonNull
    @Override
    public ResponseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_response, parent, false);
        return new ResponseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResponseViewHolder holder, int position) {
        String response = responses.get(position);
        holder.responseTextView.setText(response);
        holder.checkBox.setChecked(selectedStates.get(position));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedStates.set(position, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return responses.size();
    }

    // Method to get the selected responses
    public List<String> getSelectedResponses() {
        List<String> selectedResponses = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            if (selectedStates.get(i)) {
                selectedResponses.add(responses.get(i));
            }
        }
        return selectedResponses;
    }

    static class ResponseViewHolder extends RecyclerView.ViewHolder {
        TextView responseTextView;
        CheckBox checkBox;

        public ResponseViewHolder(@NonNull View itemView) {
            super(itemView);
            responseTextView = itemView.findViewById(R.id.responseTextView);
            checkBox = itemView.findViewById(R.id.responseCheckBox);
        }
    }
}