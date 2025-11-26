package com.example.weathertune;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private SwitchCompat switchWeatherNotification;
    private SwitchCompat switchTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 뷰 초기화
        btnBack = findViewById(R.id.btnBack);
        switchWeatherNotification = findViewById(R.id.switchWeatherNotification);
        switchTheme = findViewById(R.id.switchTheme);

        // 뒤로가기 버튼 클릭 리스너
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티 종료
            }
        });

        // 날씨 알림 스위치 리스너
        switchWeatherNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: 알림 설정 저장
            if (isChecked) {
                // 알림 활성화
            } else {
                // 알림 비활성화
            }
        });

        // 테마 스위치 리스너
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: 테마 설정 저장
            if (isChecked) {
                // 다크 모드 활성화
            } else {
                // 라이트 모드 활성화
            }
        });
    }
}