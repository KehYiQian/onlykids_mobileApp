package com.example.onlykids_mobile_application;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VideoSearchActivity extends AppCompatActivity {

    private EditText etSearchQuery;
    private MaterialButton btnSearch;
    private RecyclerView rvVideoResults;
    private RecyclerView rvRecommendedVideos;
    private LinearLayout loadingLayout;
    private TextView tvRecommendedTitle;
    private VideoAdapter videoAdapter;
    private VideoAdapter recommendedVideoAdapter;
    private List<Video> videoList;
    private List<Video> recommendedVideoList;
    private RequestQueue requestQueue;
    private static final String API_KEY = "AIzaSyBuR2PgSqW3YVTB2GiOGFN6IxRKytKMcjg";
    private static final String YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final List<String> BLOCKED_KEYWORDS = Arrays.asList(
            "adult", "sex", "sexual", "violence", "violent", "drugs", "drug", "alcohol", "gambling", "hate",
            "weapon", "weapons", "nsfw", "explicit", "horror", "crime", "murder", "kill", "killing", "fight",
            "fighting", "blood", "gore", "scary", "death", "war", "terror", "terrorism", "abuse", "bully",
            "bullying", "injury", "danger", "dangerous", "illegal", "smoke", "smoking", "vape", "vaping",
            "swear", "curse", "rude", "offensive", "inappropriate", "profane", "profanity", "racist", "sexist"
    );
    private static final List<String> PREFERRED_KEYWORDS = Arrays.asList(
            "education", "educational", "school", "primary", "kids", "children", "student", "learning", "study",
            "math", "mathematics", "science", "english", "malay", "mandarin", "history", "art", "music", "geography",
            "animals", "plants", "space", "weather", "colors", "shapes", "stories", "songs", "nursery rhymes"
    );
    private Set<String> reportedVideoIds;
    private static final String PREFS_NAME = "OnlyKidsPrefs";
    private static final String REPORTED_VIDEOS_KEY = "ReportedVideoIds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_search);

        // Load reported video IDs from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        reportedVideoIds = new HashSet<>(prefs.getStringSet(REPORTED_VIDEOS_KEY, new HashSet<>()));

        // Initialize views
        TextView tvTitle = findViewById(R.id.tv_title);
        etSearchQuery = findViewById(R.id.et_search_query);
        btnSearch = findViewById(R.id.btn_search);
        rvVideoResults = findViewById(R.id.rv_video_results);
        rvRecommendedVideos = findViewById(R.id.rv_recommended_videos);
        loadingLayout = findViewById(R.id.loading_layout);
        tvRecommendedTitle = findViewById(R.id.tv_recommended_title);

        // Initialize RecyclerView and adapter for search results
        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(this, videoList, reportedVideoIds, this::showNotification);
        rvVideoResults.setAdapter(videoAdapter);

        // Initialize RecyclerView and adapter for recommended videos
        recommendedVideoList = new ArrayList<>();
        recommendedVideoAdapter = new VideoAdapter(this, recommendedVideoList, reportedVideoIds, this::showNotification);
        rvRecommendedVideos.setAdapter(recommendedVideoAdapter);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Apply fade-in animation to the title
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvTitle.startAnimation(fadeInAnimation);

        // Apply pop-in animation to search bar and button
        Animation popInAnimation = AnimationUtils.loadAnimation(this, R.anim.pop_in);
        etSearchQuery.startAnimation(popInAnimation);
        btnSearch.startAnimation(popInAnimation);

        // Load recommended videos
        loadRecommendedVideos();
        // Initially hide search results
        rvVideoResults.setVisibility(View.GONE);

        // Search button click listener
        btnSearch.setOnClickListener(v -> {
            String query = etSearchQuery.getText().toString().trim();
            if (query.isEmpty()) {
                showNotification("Please enter a search query");
                return;
            }
            // Check for blocked keywords
            if (containsBlockedKeywords(query)) {
                showNotification("Sorry, this search term is inappropriate! Try something else. 😊");
                return;
            }
            // Check if the query has educational intent
            if (!hasEducationalIntent(query)) {
                showNotification("Please search only for educational content. 😊");
                return;
            }
            searchVideos(query);
            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save reported video IDs to SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(REPORTED_VIDEOS_KEY, reportedVideoIds);
        editor.apply();
    }

    private void showNotification(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(3000); // Auto-dismiss after 3 seconds
        snackbar.show();
    }

    private boolean containsBlockedKeywords(String text) {
        String textLower = text.toLowerCase();
        for (String keyword : BLOCKED_KEYWORDS) {
            if (textLower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEducationalIntent(String query) {
        String queryLower = query.toLowerCase();
        int score = 0;
        for (String keyword : PREFERRED_KEYWORDS) {
            if (queryLower.contains(keyword)) {
                score += 2;
            }
        }
        if (queryLower.contains("game") || queryLower.contains("funny") || queryLower.contains("prank")) {
            score -= 1;
        }
        return score >= 1;
    }

    private List<Video> filterVideos(List<Video> videos) {
        List<Video> filteredVideos = new ArrayList<>();
        for (Video video : videos) {
            String titleLower = video.getTitle().toLowerCase();
            // Exclude reported videos and videos with blocked keywords
            if (!reportedVideoIds.contains(video.getVideoId()) && !containsBlockedKeywords(titleLower)) {
                filteredVideos.add(video);
            }
        }
        return filteredVideos;
    }

    private void searchVideos(String query) {
        loadingLayout.setVisibility(View.VISIBLE);
        rvVideoResults.setVisibility(View.GONE);
        rvRecommendedVideos.setVisibility(View.GONE);
        tvRecommendedTitle.setVisibility(View.GONE);

        String url = YOUTUBE_API_URL + "?part=snippet&maxResults=10&q=" + query.replace(" ", "+") +
                "&type=video&videoCategoryId=27&videoDuration=short&videoLicense=youtube&key=" + API_KEY +
                "&safeSearch=strict";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingLayout.setVisibility(View.GONE);
                        rvVideoResults.setVisibility(View.VISIBLE);
                        rvRecommendedVideos.setVisibility(View.GONE);
                        tvRecommendedTitle.setVisibility(View.VISIBLE);
                        tvRecommendedTitle.setText("This is Your Search Result");

                        try {
                            List<Video> newVideos = new ArrayList<>();
                            JSONArray items = response.getJSONArray("items");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                JSONObject snippet = item.getJSONObject("snippet");
                                String videoId = item.getJSONObject("id").getString("videoId");
                                String title = snippet.getString("title");
                                String thumbnailUrl = snippet.getJSONObject("thumbnails")
                                        .getJSONObject("medium")
                                        .getString("url");
                                newVideos.add(new Video(title, thumbnailUrl, videoId));
                            }
                            newVideos = filterVideos(newVideos);
                            if (newVideos.isEmpty()) {
                                showNotification("No safe videos found. Please try a different search term.");
                                tvRecommendedTitle.setText("You Might Like These...");
                                tvRecommendedTitle.setVisibility(View.VISIBLE);
                                rvRecommendedVideos.setVisibility(View.VISIBLE);
                                rvVideoResults.setVisibility(View.GONE);
                            } else {
                                videoAdapter.updateVideos(newVideos);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showNotification("Error parsing video results");
                            tvRecommendedTitle.setText("You Might Like These...");
                            tvRecommendedTitle.setVisibility(View.VISIBLE);
                            rvRecommendedVideos.setVisibility(View.VISIBLE);
                            rvVideoResults.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingLayout.setVisibility(View.GONE);
                        rvVideoResults.setVisibility(View.GONE);
                        rvRecommendedVideos.setVisibility(View.VISIBLE);
                        tvRecommendedTitle.setVisibility(View.VISIBLE);
                        tvRecommendedTitle.setText("You Might Like These...");

                        error.printStackTrace();
                        String message = "Failed to fetch videos";
                        if (error.getMessage() != null) {
                            if (error.getMessage().contains("java.lang.SecurityException")) {
                                message = "Cannot fetch videos: No internet permission";
                            } else if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                                message = "Cannot fetch videos: API quota exceeded or invalid API key";
                            } else {
                                message = "Cannot fetch videos: Check your internet connection";
                            }
                        }
                        showNotification(message);
                    }
                });

        requestQueue.add(request);
    }

    private void loadRecommendedVideos() {
        String recommendedQuery = "educational videos for primary school students";

        String url = YOUTUBE_API_URL + "?part=snippet&maxResults=5&q=" + recommendedQuery.replace(" ", "+") +
                "&type=video&videoCategoryId=27&videoDuration=short&videoLicense=youtube&key=" + API_KEY +
                "&safeSearch=strict";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<Video> newVideos = new ArrayList<>();
                            JSONArray items = response.getJSONArray("items");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                JSONObject snippet = item.getJSONObject("snippet");
                                String videoId = item.getJSONObject("id").getString("videoId");
                                String title = snippet.getString("title");
                                String thumbnailUrl = snippet.getJSONObject("thumbnails")
                                        .getJSONObject("medium")
                                        .getString("url");
                                newVideos.add(new Video(title, thumbnailUrl, videoId));
                            }
                            newVideos = filterVideos(newVideos);
                            if (!newVideos.isEmpty()) {
                                recommendedVideoAdapter.updateVideos(newVideos);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showNotification("Error loading recommended videos");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        showNotification("Failed to load recommended videos");
                    }
                });

        requestQueue.add(request);
    }
}