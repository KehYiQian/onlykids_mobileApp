package com.example.onlykids_mobile_application;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.Objects;

public class chatGPT_services {

    public interface ChatGPTCallback {
        void onSuccess(String reply);
        void onError();
    }

    // For generating normal response to user
    public static void callChatGPTText(Context context, String prompt, ChatGPTCallback callback) {
        callRawChatGPT(prompt, false, callback);
    }

    // For generating quiz JSON extraction
    public static void callChatGPT(Context context, String prompt, ChatGPTCallback callback) {
        callRawChatGPT(prompt, true, callback);
    }

    //Let the chaGPT API determine the topic
    public static void classifyTopic(String userMessage, ChatGPTCallback callback) {
        String prompt = "You are a topic classifier. Determine whether the following message is related to 'Science', 'Math', or 'English'. " +
                "Only return the category name exactly as one of these three: Science, Math, English. " +
                "If it doesn't match, return 'Unknown'.\n\n" +
                "Message: \"" + userMessage + "\"";

        callRawChatGPT(prompt, false, new ChatGPTCallback() {
            @Override
            public void onSuccess(String reply) {
                // Clean result
                reply = reply.trim().toLowerCase();
                if (reply.contains("science")) {
                    callback.onSuccess("Science");
                } else if (reply.contains("math")) {
                    callback.onSuccess("Math");
                } else if (reply.contains("english")) {
                    callback.onSuccess("English");
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onError() {
                callback.onError();
            }
        });
    }

    // Internal shared logic
    private static void callRawChatGPT(String prompt, boolean expectJsonArray, ChatGPTCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject jsonBody = new JSONObject();
            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();

            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            jsonBody.put("model", "gpt-3.5-turbo");
            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json")
            );

            String API_KEY = "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" ; // your key
            String API_URL = "https://api.openai.com/v1/chat/completions";

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onError();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response)  {
                    if (!response.isSuccessful()) {
                        callback.onError();
                        return;
                    }

                    try {
                        String body = Objects.requireNonNull(response.body()).string();
                        JSONObject obj = new JSONObject(body);
                        String reply = obj.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        if (expectJsonArray) {
                            int start = reply.indexOf("[");
                            int end = reply.lastIndexOf("]") + 1;
                            if (start >= 0 && end > start) {
                                reply = reply.substring(start, end);
                                callback.onSuccess(reply);
                            } else {
                                callback.onError(); // not a valid array
                            }
                        } else {
                            callback.onSuccess(reply); // return plain text
                        }
                    } catch (Exception e) {
                        callback.onError();
                    }
                }
            });

        } catch (Exception e) {
            callback.onError();
        }
    }
}



