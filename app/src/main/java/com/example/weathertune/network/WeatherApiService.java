package com.example.weathertune.network;

import com.example.weathertune.network.dto.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("data/3.0/onecall")
    Call<WeatherResponse> getWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("exclude") String exclude,
            @Query("units") String units,
            @Query("lang") String lang,
            @Query("appid") String apiKey
    );
}