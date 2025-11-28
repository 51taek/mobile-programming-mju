package com.example.weathertune;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private SwitchCompat switchWeatherNotification;
    private SwitchCompat switchTheme;
    private ImageView themeIcon;
    private TextView themeLabel;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "WeatherTuneSettings";
    private static final String KEY_WEATHER_NOTIFICATION = "weather_notification";
    private static final String KEY_DARK_MODE = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // SharedPreferences 초기화
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // 뷰 초기화
        btnBack = findViewById(R.id.btnBack);
        switchWeatherNotification = findViewById(R.id.switchWeatherNotification);
        switchTheme = findViewById(R.id.switchTheme);
        themeIcon = findViewById(R.id.themeIcon);
        themeLabel = findViewById(R.id.themeLabel);

        // 저장된 알림 설정 불러오기 (기본값: true)
        boolean isNotificationEnabled = preferences.getBoolean(KEY_WEATHER_NOTIFICATION, true);
        boolean isDarkMode = preferences.getBoolean(KEY_DARK_MODE, false);

        switchWeatherNotification.setChecked(isNotificationEnabled);
        switchTheme.setChecked(isDarkMode);

        // 초기 아이콘과 텍스트 설정
        updateThemeIconAndText(isDarkMode);

        // 뒤로가기 버튼 클릭 리스너
        btnBack.setOnClickListener(v -> finish());

        // 날씨 알림 스위치 리스너
        switchWeatherNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 설정 저장
            preferences.edit().putBoolean(KEY_WEATHER_NOTIFICATION, isChecked).apply();

            // 배너 표시
            if (isChecked) {
                showCustomBanner("날씨 알림 켜짐\n날씨 변화 시 음악 추천 알림을 받습니다.", "#757575");
            } else {
                showCustomBanner("날씨 알림 꺼짐\n날씨 알림을 받지 않습니다.", "#757575");
            }
        });

        // 테마 스위치 리스너
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 설정 저장
            preferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

            // 아이콘과 텍스트 업데이트
            updateThemeIconAndText(isChecked);

            // 테마 전환
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                showCustomBanner("다크 모드 켜짐\n어두운 테마가 적용되었습니다.", "#757575");
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                showCustomBanner("라이트 모드 켜짐\n밝은 테마가 적용되었습니다.", "#757575");
            }

            // 현재 액티비티만 재시작
            recreate();
        });
    }

    // 테마 아이콘과 텍스트 업데이트
    private void updateThemeIconAndText(boolean isDarkMode) {
        if (isDarkMode) {
            themeIcon.setImageResource(R.drawable.ic_moon);
            themeLabel.setText("다크 모드");
        } else {
            themeIcon.setImageResource(R.drawable.ic_sun);
            themeLabel.setText("라이트 모드");
        }
    }

    @SuppressLint("RestrictedApi")
    private void showCustomBanner(String message, String backgroundColor) {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "",
                Snackbar.LENGTH_SHORT
        );

        // Snackbar 레이아웃 커스터마이징
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        // 기본 텍스트뷰 숨기기
        TextView textView = layout.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        // 커스텀 뷰 생성
        View customView = LayoutInflater.from(this).inflate(
                R.layout.custom_banner_layout,
                null
        );

        // 커스텀 뷰의 텍스트와 배경색 설정
        TextView bannerText = customView.findViewById(R.id.banner_text);
        bannerText.setText(message);
        customView.setBackgroundColor(android.graphics.Color.parseColor(backgroundColor));

        if (customView instanceof androidx.cardview.widget.CardView) {
            ((androidx.cardview.widget.CardView) customView).setRadius(24); // 픽셀 단위
        }

        // 레이아웃에 커스텀 뷰 추가
        layout.setPadding(0, 0, 0, 0);
        layout.addView(customView, 0);

        // 상단에 표시되도록 설정
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.topMargin = 80; // 상단 여백
        layout.setLayoutParams(params);

        snackbar.show();
    }
}