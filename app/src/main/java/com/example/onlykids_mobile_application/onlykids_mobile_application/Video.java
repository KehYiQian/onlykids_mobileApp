package com.example.onlykids_mobile_application.onlykids_mobile_application;

public class Video {
    private String title;
    private String thumbnailUrl;
    private String videoId;

    public Video(String title, String thumbnailUrl, String videoId) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getVideoId() {
        return videoId;
    }
}