package com.example.weathertune.youtube;

public class MusicItem {
    private String videoId;
    private String title;
    private String thumbnailUrl;
    private String channelTitle;

    public MusicItem(String videoId, String title, String thumbnailUrl, String channelTitle) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.channelTitle = channelTitle;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public String getYouTubeUrl() {
        return "https://www.youtube.com/watch?v=" + videoId;
    }
}
