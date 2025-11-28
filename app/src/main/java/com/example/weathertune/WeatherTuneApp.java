package com.example.weathertune;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class WeatherTuneApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 앱 시작 시 저장된 테마 설정 적용
        SharedPreferences preferences = getSharedPreferences("WeatherTuneSettings", MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}