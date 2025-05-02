package com.example.onlykids;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesActivity extends AppCompatActivity {
    private RecyclerView responsesRecyclerView;
    private ResponseAdapter responseAdapter;
    private Button saveToAppButton;
    private Button viewNotesButton;
    private Button clearChatButton;
    private ImageButton backToChatButton;
    private List<String> assistantResponses;
    private SharedPreferences sharedPreferences;

    public static final int REQUEST_CLEAR_CHAT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        responsesRecyclerView = findViewById(R.id.responsesRecyclerView);
        saveToAppButton = findViewById(R.id.saveToAppButton);
        viewNotesButton = findViewById(R.id.viewNotesButton);
        clearChatButton = findViewById(R.id.clearChatButton);
        backToChatButton = findViewById(R.id.backToChatButton);

        sharedPreferences = getSharedPreferences("OnlyKidsNotes", MODE_PRIVATE);

        // Get the assistant responses passed from MainActivity
        assistantResponses = getIntent().getStringArrayListExtra("assistant_responses");
        if (assistantResponses == null) {
            assistantResponses = new ArrayList<>();
        }

        // Set up the RecyclerView with the ResponseAdapter
        responseAdapter = new ResponseAdapter(assistantResponses);
        responsesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        responsesRecyclerView.setAdapter(responseAdapter);

        // Handle the "SAVE TO APP" button click
        saveToAppButton.setOnClickListener(v -> {
            List<String> selectedResponses = responseAdapter.getSelectedResponses();
            if (!selectedResponses.isEmpty()) {
                saveSelectedResponses(selectedResponses);
                showNavigationDialog();
            }
        });

        // Handle the "VIEW SAVED NOTES" button click
        viewNotesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewNotesActivity.class);
            startActivity(intent);
        });

        // Handle the "CLEAR CHAT HISTORY" button click
        clearChatButton.setOnClickListener(v -> {
            showClearChatConfirmationDialog();
        });

        // Handle the "BACK TO CHATBOX" button click
        backToChatButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void saveSelectedResponses(List<String> selectedResponses) {
        // Load existing saved notes from SharedPreferences
        String savedNotesJson = sharedPreferences.getString("saved_notes", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<SavedNote>>(){}.getType();
        List<SavedNote> savedNotes = gson.fromJson(savedNotesJson, type);
        if (savedNotes == null) {
            savedNotes = new ArrayList<>();
        }

        // Add the selected responses with timestamps
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        for (String response : selectedResponses) {
            savedNotes.add(new SavedNote(response, currentTime));
        }

        // Save the updated notes back to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("saved_notes", gson.toJson(savedNotes));
        editor.apply();
    }

    private void showNavigationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notes Saved!")
                .setMessage("Your notes have been saved. Do you want to view them now or stay on this page?")
                .setPositiveButton("View Saved Notes", (dialog, which) -> {
                    Intent intent = new Intent(this, ViewNotesActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Stay Here", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void showClearChatConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat History")
                .setMessage("Are you sure you want to clear all chat history? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    clearChatHistory();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void clearChatHistory() {
        // Clear the assistant responses in NotesActivity
        assistantResponses.clear();
        responseAdapter.notifyDataSetChanged();

        // Send result back to MainActivity to clear chat history
        Intent resultIntent = new Intent();
        setResult(REQUEST_CLEAR_CHAT, resultIntent);
        finish();
    }
}

// Helper class to store a note with its timestamp
class SavedNote {
    private String note;
    private String timestamp;

    public SavedNote(String note, String timestamp) {
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }
}