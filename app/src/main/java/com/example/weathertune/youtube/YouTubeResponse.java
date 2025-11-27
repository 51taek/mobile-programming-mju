package com.example.weathertune.youtube;

import java.util.List;

public class YouTubeResponse {
    public List<Item> items;

    public static class Item {
        public String kind;
        public String etag;
        public Id id;
        public Snippet snippet;
    }

    public static class Id {
        public String kind;
        public String videoId;
    }

    public static class Snippet {
        public String publishedAt;
        public String channelId;
        public String title;
        public String description;
        public Thumbnails thumbnails;
        public String channelTitle;
    }

    public static class Thumbnails {
        public Thumbnail defaultThumbnail;  // "default"는 예약어라서 변수명 변경
        public Thumbnail medium;
        public Thumbnail high;
    }

    public static class Thumbnail {
        public String url;
        public int width;
        public int height;
    }
}
