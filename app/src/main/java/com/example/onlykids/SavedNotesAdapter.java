package com.example.onlykids;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SavedNotesAdapter extends RecyclerView.Adapter<SavedNotesAdapter.SavedNoteViewHolder> {
    private List<SavedNote> savedNotes;
    private OnDeleteClickListener deleteClickListener;

    // Interface for delete button click callback
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public SavedNotesAdapter(List<SavedNote> savedNotes, OnDeleteClickListener listener) {
        this.savedNotes = new ArrayList<>(savedNotes);
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public SavedNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_note, parent, false);
        return new SavedNoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedNoteViewHolder holder, int position) {
        SavedNote savedNote = savedNotes.get(position);
        holder.timestampTextView.setText("Saved on: " + savedNote.getTimestamp());
        holder.noteTextView.setText(savedNote.getNote());
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedNotes.size();
    }

    // Method to update the list of notes (for filtering and sorting)
    public void updateNotes(List<SavedNote> newNotes) {
        this.savedNotes.clear();
        this.savedNotes.addAll(newNotes);
        notifyDataSetChanged();
    }

    // Method to remove a note at a specific position
    public void removeNote(int position) {
        savedNotes.remove(position);
        notifyItemRemoved(position);
    }

    // Method to get the note at a specific position
    public SavedNote getNote(int position) {
        return savedNotes.get(position);
    }

    static class SavedNoteViewHolder extends RecyclerView.ViewHolder {
        TextView timestampTextView;
        TextView noteTextView;
        Button deleteButton;

        public SavedNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}