package com.example.onlykids;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SavedNotesAdapter extends RecyclerView.Adapter<SavedNotesAdapter.SavedNoteViewHolder> {
    private List<SavedNote> savedNotes;
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;
    private final android.content.Context context;
//asdasd
    // Interfaces for callback
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public SavedNotesAdapter(List<SavedNote> savedNotes, OnDeleteClickListener deleteListener, OnEditClickListener editListener, android.content.Context context) {
        this.savedNotes = new ArrayList<>(savedNotes);
        this.deleteClickListener = deleteListener;
        this.editClickListener = editListener;
        this.context = context;
    }

    @NonNull
    @Override
    public SavedNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_notes, parent, false);
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
        holder.editButton.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(position);
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
        ImageButton deleteButton;
        ImageButton editButton;

        public SavedNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
}