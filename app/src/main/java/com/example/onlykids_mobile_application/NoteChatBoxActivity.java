package com.example.onlykids_mobile_application;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteChatBoxActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private Button notesButton;
    private ImageButton uploadButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<String> filePickerLauncher;
    private String uploadedFileContent = "";
    private static final String TAG = "MainActivity";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int SETTINGS_REQUEST_CODE = 101;
    private static final int REQUEST_NOTES = 2;

    private static final Dns CUSTOM_DNS = new Dns() {
        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            try {
                InetAddress[] addresses = InetAddress.getAllByName(hostname);
                List<InetAddress> result = new ArrayList<>();
                Collections.addAll(result, addresses);
                Log.d(TAG, "Custom DNS resolved " + hostname + " to: " + result);
                return result;
            } catch (UnknownHostException e) {
                Log.e(TAG, "Custom DNS failed to resolve " + hostname + ": " + e.getMessage(), e);
                throw e;
            }
        }
    };

    interface OnTextExtractedCallback {
        void onTextExtracted(String text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        notesButton = findViewById(R.id.notesButton);
        uploadButton = findViewById(R.id.uploadButton);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        sharedPreferences = getSharedPreferences("OnlyKidsNotes", MODE_PRIVATE);

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            String mimeType = getContentResolver().getType(uri);
                            if (mimeType != null) {
                                if (mimeType.equals("image/jpeg") || mimeType.equals("image/png")) {
                                    extractTextFromImage(uri, extractedText -> {
                                        uploadedFileContent = extractedText;
                                        chatMessages.add(new ChatMessage("Image uploaded successfully! (JPG/PNG)", false));
                                        if (extractedText.startsWith("Error") || extractedText.equals("No text found in the image.")) {
                                            chatMessages.add(new ChatMessage(extractedText, false));
                                        }
                                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                                    });
                                } else {
                                    chatMessages.add(new ChatMessage("Unsupported file type. Please upload a JPG or PNG.", false));
                                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                                }
                            } else {
                                chatMessages.add(new ChatMessage("Error: Could not determine file type.", false));
                                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                            }
                        } catch (Exception e) {
                            chatMessages.add(new ChatMessage("Error uploading file: " + e.getMessage(), false));
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                        }
                    }
                });

        sendButton.setOnClickListener(v -> {
            Log.d(TAG, "Send button clicked");
            String messageText = messageEditText.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });

        uploadButton.setOnClickListener(v -> {
            checkStoragePermission();
        });

        notesButton.setOnClickListener(v -> openNotes());

        if (API_KEY == null || API_KEY.trim().isEmpty() || !API_KEY.startsWith("sk-")) {
            chatMessages.add(new ChatMessage("Error: Invalid API key format. Please set a valid OpenAI API key in MainActivity.java.", false));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        } else {
            Log.d(TAG, "API Key set: " + API_KEY.substring(0, 5) + "... (partially hidden for security)");
        }

        chatMessages.add(new ChatMessage("Hi there! How can I help you today?", false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        // Request focus and show keyboard with delay
        new Handler().postDelayed(() -> {
            Log.d(TAG, "Requesting focus on messageEditText");
            messageEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                Log.d(TAG, "Showing soft keyboard");
                imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
                if (!imm.isActive()) {
                    Log.w(TAG, "Soft keyboard not active, attempting to force show");
                    messageEditText.post(() -> imm.showSoftInput(messageEditText, InputMethodManager.SHOW_FORCED));
                }
            } else {
                Log.e(TAG, "InputMethodManager is null");
            }
        }, 200); // Delay to allow UI to settle
    }

    private void checkStoragePermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                chatMessages.add(new ChatMessage("We need storage access to upload your files. Please allow the permission.", false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
            } else if (sharedPreferences.getBoolean("permission_rationale_shown", false)) {
                chatMessages.add(new ChatMessage("Storage permission is required to upload files. Please enable it in app settings.", false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("permission_rationale_shown", true);
                editor.apply();
            }
        } else {
            filePickerLauncher.launch("image/*");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                filePickerLauncher.launch("image/*");
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    chatMessages.add(new ChatMessage("Storage permission is required to upload files. Please enable it in app settings.", false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, SETTINGS_REQUEST_CODE);
                } else {
                    chatMessages.add(new ChatMessage("Permission denied. Please allow storage access to upload files.", false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE) {
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                filePickerLauncher.launch("image/*");
            } else {
                chatMessages.add(new ChatMessage("Storage permission is still denied. Please allow it to upload files.", false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        } else if (requestCode == REQUEST_NOTES && resultCode == NotesActivity.REQUEST_CLEAR_CHAT) {
            // Clear chat history
            chatMessages.clear();
            chatAdapter.notifyDataSetChanged();
            chatRecyclerView.scrollToPosition(0);
            // Add a welcome message after clearing
            chatMessages.add(new ChatMessage("Chat history cleared. How can I help you today?", false));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        }
    }

    private void extractTextFromImage(Uri uri, OnTextExtractedCallback callback) {
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(text -> {
                        String extractedText = text.getText();
                        if (extractedText.isEmpty()) {
                            callback.onTextExtracted("No text found in the image.");
                        } else {
                            callback.onTextExtracted(extractedText);
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onTextExtracted("Error extracting text from image: " + e.getMessage());
                    });
        } catch (Exception e) {
            callback.onTextExtracted("Error processing image: " + e.getMessage());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnected();
        Log.d(TAG, "Network available: " + isConnected);
        if (isConnected) {
            Log.d(TAG, "Network type: " + networkInfo.getTypeName());
            Log.d(TAG, "Network state: " + networkInfo.getState());
        }
        return isConnected;
    }

    private void sendMessage(String messageText) {
        chatMessages.add(new ChatMessage(messageText, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        if (!isNetworkAvailable()) {
            runOnUiThread(() -> {
                chatMessages.add(new ChatMessage("Error: No internet connection. Please check your network and try again.", false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            });
            return;
        }

        new Thread(() -> {
            try {
                String botResponse = getChatGPTResponseWithRetry(messageText);
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage(botResponse, false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    messageEditText.setText("");
                    generateAndSaveNotes();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("Error: API call failed after all retries: " + e.getMessage() + ". Please try again later.", false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    Log.e(TAG, "API call failed after all retries: " + e.getMessage(), e);
                });
            }
        }).start();
    }

    private String getChatGPTResponseWithRetry(String userMessage) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .dns(CUSTOM_DNS)
                .connectTimeout(30, TimeUnit.SECONDS) // Increased timeout
                .readTimeout(30, TimeUnit.SECONDS)   // Increased timeout
                .writeTimeout(30, TimeUnit.SECONDS)  // Increased timeout
                .build();

        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "You are a helpful assistant for primary school students. Respond in a simple, clear, and friendly way suitable for young children. Always consider the full conversation history and respond to the latest user message in context."));

        int startIndex = Math.max(0, chatMessages.size() - 5);
        for (int i = startIndex; i < chatMessages.size(); i++) {
            ChatMessage chatMessage = chatMessages.get(i);
            messages.put(new JSONObject()
                    .put("role", chatMessage.isUserMessage() ? "user" : "assistant")
                    .put("content", chatMessage.getMessage()));
        }

        if (!uploadedFileContent.isEmpty() && uploadedFileContent.length() < 5000) {
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", "Uploaded file content: " + uploadedFileContent));
        } else if (!uploadedFileContent.isEmpty()) {
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", "Uploaded file content is too large to include directly. Please refer to the user's message for context."));
        }

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userMessage));

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", "gpt-3.5-turbo");
        jsonBody.put("messages", messages);
        jsonBody.put("temperature", 0.7);

        Log.d(TAG, "Request body: " + jsonBody.toString());
        String authHeader = "Bearer " + API_KEY;
        Log.d(TAG, "Authorization header: " + authHeader.substring(0, 15) + "... (partially hidden for security)");

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Log.d(TAG, "Attempt " + attempt + " of " + MAX_RETRIES + " to send request to: " + API_URL);
                try (Response response = client.newCall(request).execute()) {
                    Log.d(TAG, "Received response for attempt " + attempt);
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    Log.d(TAG, "Response code: " + response.code());
                    Log.d(TAG, "Response body: " + responseBody);

                    if (!response.isSuccessful()) {
                        String errorMessage = response.message();
                        try {
                            JSONObject errorResponse = new JSONObject(responseBody);
                            if (errorResponse.has("error")) {
                                JSONObject error = errorResponse.getJSONObject("error");
                                errorMessage = error.optString("message", response.message());
                                if (errorMessage.contains("rate_limit_exceeded")) {
                                    String retryAfterStr = errorMessage.split("Please try again in ")[1].split("s")[0];
                                    float retryAfterSeconds = Float.parseFloat(retryAfterStr);
                                    long retryAfterMillis = (long) (retryAfterSeconds * 1000);
                                    Log.d(TAG, "Rate limit exceeded, retrying after " + retryAfterMillis + "ms");
                                    Thread.sleep(retryAfterMillis);
                                    continue;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse error response: " + e.getMessage(), e);
                        }
                        throw new Exception("API call failed: " + errorMessage);
                    }
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                }
            } catch (Exception e) {
                lastException = e;
                Log.e(TAG, "Attempt " + attempt + " failed: " + e.getMessage(), e);
                if (attempt < MAX_RETRIES && !e.getMessage().contains("rate_limit_exceeded")) {
                    try {
                        Log.d(TAG, "Retrying after delay of " + RETRY_DELAY_MS + "ms");
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        Log.e(TAG, "Retry interrupted: " + ie.getMessage(), ie);
                    }
                }
            }
        }
        throw lastException != null ? lastException : new Exception("API call failed after " + MAX_RETRIES + " attempts");
    }

    private void generateAndSaveNotes() {
        if (!isNetworkAvailable()) {
            runOnUiThread(() -> {
                chatMessages.add(new ChatMessage("Error: No internet connection. Cannot generate notes.", false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            });
            return;
        }

        new Thread(() -> {
            try {
                JSONArray messages = new JSONArray();
                messages.put(new JSONObject()
                        .put("role", "system")
                        .put("content", "You are a helpful assistant for primary school students. Generate simple notes based on the conversation history and uploaded file content. Make the notes clear, concise, and easy to understand for young children."));

                int startIndex = Math.max(0, chatMessages.size() - 5);
                for (int i = startIndex; i < chatMessages.size(); i++) {
                    ChatMessage chatMessage = chatMessages.get(i);
                    messages.put(new JSONObject()
                            .put("role", chatMessage.isUserMessage() ? "user" : "assistant")
                            .put("content", chatMessage.getMessage()));
                }

                if (!uploadedFileContent.isEmpty() && uploadedFileContent.length() < 5000) {
                    messages.put(new JSONObject()
                            .put("role", "user")
                            .put("content", "Uploaded file content: " + uploadedFileContent));
                } else if (!uploadedFileContent.isEmpty()) {
                    messages.put(new JSONObject()
                            .put("role", "user")
                            .put("content", "Uploaded file content is too large to include directly. Please summarize based on the conversation."));
                }

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", "gpt-3.5-turbo");
                jsonBody.put("messages", messages);
                jsonBody.put("temperature", 0.7);

                OkHttpClient client = new OkHttpClient.Builder()
                        .dns(CUSTOM_DNS)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();

                MediaType JSON = MediaType.get("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
                Request request = new Request.Builder()
                        .url(API_URL)
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    if (!response.isSuccessful()) {
                        throw new Exception("API call failed: " + response.message() + " - " + responseBody);
                    }
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String notes = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("generated_notes", notes);
                    editor.apply();
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("Error generating notes: " + e.getMessage(), false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                    Log.e(TAG, "Notes generation failed: " + e.getMessage(), e);
                });
            }
        }).start();
    }

    private void openNotes() {
        ArrayList<String> assistantResponses = new ArrayList<>();
        for (ChatMessage message : chatMessages) {
            if (!message.isUserMessage()) {
                assistantResponses.add(message.getMessage());
            }
        }

        Intent intent = new Intent(this, NotesActivity.class);
        intent.putStringArrayListExtra("assistant_responses", assistantResponses);
        startActivityForResult(intent, REQUEST_NOTES);
    }
}