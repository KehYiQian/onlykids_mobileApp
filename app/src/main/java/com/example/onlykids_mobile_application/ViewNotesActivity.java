package com.example.onlykids_mobile_application;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ViewNotesActivity extends AppCompatActivity implements SavedNotesAdapter.OnDeleteClickListener, SavedNotesAdapter.OnEditClickListener {
    private RecyclerView savedNotesRecyclerView;
    private SavedNotesAdapter savedNotesAdapter;
    private SharedPreferences sharedPreferences;
    private List<SavedNote> allSavedNotes; // Store all notes for filtering
    private TextView selectedDateTextView;
    private Button selectDateButton;
    private Button clearFilterButton;
    private SimpleDateFormat dateFormat;
    private String currentFilterDate; // Store the currently applied date filter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);

        // Initialize UI components
        savedNotesRecyclerView = findViewById(R.id.savedNotesRecyclerView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        selectDateButton = findViewById(R.id.selectDateButton);
        clearFilterButton = findViewById(R.id.clearFilterButton);
        sharedPreferences = getSharedPreferences("OnlyKidsNotes", MODE_PRIVATE);

        // Date format for filtering (only the date part, excluding time)
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentFilterDate = null; // Initially, no date filter is applied

        // Load all saved notes from SharedPreferences
        String savedNotesJson = sharedPreferences.getString("saved_notes", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<SavedNote>>(){}.getType();
        allSavedNotes = gson.fromJson(savedNotesJson, type);
        if (allSavedNotes == null) {
            allSavedNotes = new ArrayList<>();
        }

        // Sort all notes by timestamp (newest to oldest)
        sortNotesByTimestamp(allSavedNotes);

        // Set up the RecyclerView with all notes initially
        savedNotesAdapter = new SavedNotesAdapter(allSavedNotes, this, this, this);
        savedNotesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        savedNotesRecyclerView.setAdapter(savedNotesAdapter);

        // Set up the date picker button
        selectDateButton.setOnClickListener(v -> showDatePickerDialog());

        // Set up the clear filter button
        clearFilterButton.setOnClickListener(v -> clearFilter());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    String selectedDateString = dateFormat.format(selectedDate.getTime());
                    selectedDateTextView.setText("Selected Date: " + selectedDateString);

                    currentFilterDate = selectedDateString;
                    filterNotesByDate(selectedDateString);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void filterNotesByDate(String selectedDate) {
        List<SavedNote> filteredNotes = new ArrayList<>();
        for (SavedNote note : allSavedNotes) {
            String noteDate = note.getTimestamp().substring(0, 10);
            if (noteDate.equals(selectedDate)) {
                filteredNotes.add(note);
            }
        }

        sortNotesByTimestamp(filteredNotes);
        savedNotesAdapter.updateNotes(filteredNotes);
    }

    private void clearFilter() {
        currentFilterDate = null;
        selectedDateTextView.setText("Select a date to filter notes");
        sortNotesByTimestamp(allSavedNotes);
        savedNotesAdapter.updateNotes(allSavedNotes);
    }

    private void sortNotesByTimestamp(List<SavedNote> notes) {
        Collections.sort(notes, new Comparator<SavedNote>() {
            @Override
            public int compare(SavedNote note1, SavedNote note2) {
                return note2.getTimestamp().compareTo(note1.getTimestamp());
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        SavedNote noteToDelete = savedNotesAdapter.getNote(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    allSavedNotes.removeIf(note -> note.getNote().equals(noteToDelete.getNote()) && note.getTimestamp().equals(noteToDelete.getTimestamp()));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("saved_notes", new Gson().toJson(allSavedNotes));
                    editor.apply();

                    if (currentFilterDate != null) {
                        filterNotesByDate(currentFilterDate);
                    } else {
                        sortNotesByTimestamp(allSavedNotes);
                        savedNotesAdapter.updateNotes(allSavedNotes);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    @Override
    public void onEditClick(int position) {
        SavedNote noteToEdit = savedNotesAdapter.getNote(position);

        // Create an EditText for the user to modify the note
        EditText editText = new EditText(this);
        editText.setText(noteToEdit.getNote());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setLines(3);
        editText.setMaxLines(5);

        new AlertDialog.Builder(this)
                .setTitle("Edit Note")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updatedNote = editText.getText().toString().trim();
                    if (!updatedNote.isEmpty()) {
                        // Update the note in allSavedNotes
                        for (SavedNote note : allSavedNotes) {
                            if (note.getNote().equals(noteToEdit.getNote()) && note.getTimestamp().equals(noteToEdit.getTimestamp())) {
                                note.setNote(updatedNote); // Assuming SavedNote has a setNote method
                                break;
                            }
                        }

                        // Update SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("saved_notes", new Gson().toJson(allSavedNotes));
                        editor.apply();

                        // Refresh the RecyclerView
                        if (currentFilterDate != null) {
                            filterNotesByDate(currentFilterDate);
                        } else {
                            sortNotesByTimestamp(allSavedNotes);
                            savedNotesAdapter.updateNotes(allSavedNotes);
                        }
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}