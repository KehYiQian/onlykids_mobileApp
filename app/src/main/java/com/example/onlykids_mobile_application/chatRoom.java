package com.example.onlykids_mobile_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class chatRoom extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private chatRoom_adapter chatAdapter;
    private final List<chatRoom_model> chatMessages = new ArrayList<>();

    private String lastUserMessage = "";
    private String lastQuizJson = "";
    private static final String PREFS_NAME = "chat_preferences";
    private static final String KEY_CHAT_HISTORY = "chat_history";

    private AlertDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Toolbar toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Chat Room");

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        Button sendBtn = findViewById(R.id.sendBtn);
        Button quizBtn = findViewById(R.id.quizBtn);

        loadChatMessages(); // load before adapter setup

        chatAdapter = new chatRoom_adapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // When starting to load
        loadingDialog = dialog_loading_helper.showLoadingDialog(chatRoom.this, "⌛ Waiting for ChatGPT...");
        // Hide the dialog after response)
        dialog_loading_helper.hideLoadingDialog(loadingDialog);

        sendBtn.setOnClickListener(v -> {
            String question = messageInput.getText().toString();
            if (!question.isEmpty()) {
                sendMessage(question);
                messageInput.setText("");
            }
        });

        quizBtn.setOnClickListener(v -> {
            // Trigger quiz generation manually
            sendMessage("quiz");
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_room_menu, menu);
        return true;
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear) {

            if (chatMessages.isEmpty()) {
                Toast.makeText(this, "❌ No chat to clear!", Toast.LENGTH_SHORT).show();
                return true;
            }

            // Save a backup before clearing for undo
            List<chatRoom_model> backupMessages = new ArrayList<>(chatMessages);

            chatMessages.clear();
            saveChatMessages();
            chatAdapter.notifyDataSetChanged();

            Snackbar.make(chatRecyclerView, "✅ Chat cleared", Snackbar.LENGTH_LONG)
                    .setAction("Undo", v -> {
                        // Restore backup
                        chatMessages.addAll(backupMessages);
                        saveChatMessages();
                        chatAdapter.notifyDataSetChanged();
                    })
                    .show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(String message) {
        chatMessages.add(new chatRoom_model(message, "user"));
        saveChatMessages();
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        if (message.trim().equalsIgnoreCase("quiz")) {
            if (lastUserMessage.isEmpty()) {
                chatMessages.add(new chatRoom_model("⚠️ Please ask a quiz topic first.", "bot"));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                return;
            }

            loadingDialog.show();

            chatGPT_services.classifyTopic(lastUserMessage, new chatGPT_services.ChatGPTCallback() {
                @Override
                public void onSuccess(String classifiedTopic) {
                    generateLevelOneQuiz(lastUserMessage, classifiedTopic); // Only the topic science, math, and english can generate the quiz
                }

                @Override
                public void onError() {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        chatMessages.add(new chatRoom_model("❌ Only Science, Math, or English topics are supported for quizzes.", "bot"));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    });
                }
            });

        } else {
            lastUserMessage = message;

            // Let ChatGPT answer general questions (not generated quiz)
            chatGPT_services.callChatGPTText(chatRoom.this, lastUserMessage, new chatGPT_services.ChatGPTCallback() {
                @Override
                public void onSuccess(String reply) {
                    runOnUiThread(() -> {
                        chatMessages.add(new chatRoom_model(reply.trim(), "bot"));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    });
                }

                @Override
                public void onError() {
                    runOnUiThread(() ->
                            Snackbar.make(chatRecyclerView, "❌ Failed to get response.", Snackbar.LENGTH_LONG).show()
                    );
                }
            });
        }
    }

    private void generateLevelOneQuiz(String userQuestion, String topic) {
        loadingDialog.show();

        String prompt = "You are a quiz generator for primary school students. Focused on the " +  userQuestion + " with topic \"" + topic + "\", create an EASY level JSON array with exactly 3 multiple-choice questions. " +
                "Each question should be an object containing 'question', 'options' (an array of 4 choices that one of them contains answer), and 'answer'. " +
                "Ensure the complexity reflects the following guidelines:\n" +
                "- EASY: use simple, direct questions with basic facts or one-digit numbers.\n" +
                "Ensure all questions are strictly related to \"" + userQuestion + "\". " +
                "Return only a valid JSON array. Do not include explanations or text outside the array.";

        chatGPT_services.callChatGPT(chatRoom.this, prompt, new chatGPT_services.ChatGPTCallback() {
            @Override
            public void onSuccess(String reply) {
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    lastQuizJson = reply;

                    Intent intent = new Intent(chatRoom.this, start_quiz.class);
                    intent.putExtra("quizJson", lastQuizJson);
                    intent.putExtra("currentLevel", 1);
                    intent.putExtra("topic", topic);
                    intent.putExtra("userQuestion", userQuestion);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError() {
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Snackbar.make(chatRecyclerView, "❌ Failed to generate quiz from chatGPT.", Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    // Save the previous conservation in the SharedPreferences
    private void saveChatMessages() {
        JSONArray jsonArray = new JSONArray();
        for (chatRoom_model model : chatMessages) {
            try {
                jsonArray.put(model.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_CHAT_HISTORY, jsonArray.toString())
                .apply();
    }

    // Load the previous conservation from the SharedPreferences
    private void loadChatMessages() {
        chatMessages.clear();
        String savedJson = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_CHAT_HISTORY, null);

        if (savedJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(savedJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    chatMessages.add(chatRoom_model.fromJson(jsonArray.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
