package com.example.weathertune.youtube;



import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YouTubeApiService {

    @GET("youtube/v3/search")
    Call<YouTubeResponse> searchVideos(
            @Query("part") String part,           // "snippet"
            @Query("q") String query,             // 검색어
            @Query("type") String type,           // "video"
            @Query("maxResults") int maxResults,  // 결과 개수
            @Query("regionCode") String regionCode, // 지역 코드
            @Query("relevanceLanguage") String relevanceLanguage, // 언어 필터
            @Query("key") String apiKey           // YouTube API Key
    );
}