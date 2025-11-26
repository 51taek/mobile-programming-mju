package com.example.weathertune;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weathertune.network.RetrofitClient;
import com.example.weathertune.network.WeatherApiService;
import com.example.weathertune.network.dto.WeatherResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 1000;
    private static final int MAP_REQUEST_CODE = 200;
    private static final String API_KEY = BuildConfig.WEATHER_API_KEY;

    private TextView tvLocation, tvTemperature, tvWeatherDescription, tvRainProbability;
    private ImageView ivWeatherIcon, refreshBtn, gpsBtn;

    FusedLocationProviderClient fusedLocationClient;

    double selectedLat = -1;
    double selectedLon = -1;
    String selectedAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocation = findViewById(R.id.tvLocation);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherDescription = findViewById(R.id.tvWeatherDescription);
        tvRainProbability = findViewById(R.id.tvRainProbability);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        refreshBtn = findViewById(R.id.refresh_btn);
        gpsBtn = findViewById(R.id.gps_btn);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        addPressAnimation(refreshBtn);
        addPressAnimation(gpsBtn);

        refreshBtn.setOnClickListener(v -> {
            refreshBtn.animate().rotationBy(360f).setDuration(800).start();
            getWeather();
        });

        gpsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
        } else getWeather();
    }

    private void getWeather() {

        // 지도에서 선택된 위치가 있다면 그 위치 기준으로 API 요청
        if (selectedLat != -1 && selectedLon != -1) {
            requestWeather(selectedLat, selectedLon, selectedAddress);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {

            double lat;
            double lon;

            if (location == null) {
                tvLocation.setText("위치 탐색 중...");
                return;
            } else {
                lat = location.getLatitude();
                lon = location.getLongitude();
                selectedAddress = getAddress(lat, lon);
            }

            selectedLat = lat;
            selectedLon = lon;

            requestWeather(lat, lon, selectedAddress);
        });
    }

    private void requestWeather(double lat, double lon, String address) {

        WeatherApiService api = RetrofitClient
                .getInstance(this)
                .create(WeatherApiService.class);

        Call<WeatherResponse> call = api.getWeather(
                lat,
                lon,
                "minutely,daily,alerts",
                "metric",
                "kr",
                API_KEY
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    tvWeatherDescription.setText("데이터 오류");
                    return;
                }

                WeatherResponse data = response.body();

                // ================================
                // 1) 현재 온도
                // ================================
                double temp = data.current.temp;

                // ================================
                // 2) 현재 날씨 아이콘/설명
                // ================================
                String main = data.current.weather.get(0).main;
                String desc = data.current.weather.get(0).description;

                // ================================
                // 3) POP(강수확률)
                //    → 현재 시간(dt) 이후 가장 가까운 hourly 값
                // ================================
                long currentDt = data.current.dt;
                double pop = 0;

                for (WeatherResponse.Hourly h : data.hourly) {
                    if (h.dt >= currentDt) {
                        pop = h.pop * 100;
                        break;
                    }
                }

                // ================================
                // 4) UI 반영
                // ================================
                tvLocation.setText(address);
                tvTemperature.setText((int) temp + "°");
                tvWeatherDescription.setText(desc);
                tvRainProbability.setText((int) pop + "%");

                setWeatherIcon(convertWeather(main));
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvWeatherDescription.setText("네트워크 오류");
            }
        });
    }



    private String getAddress(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.KOREAN);
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);
            return list != null && !list.isEmpty() ? list.get(0).getAddressLine(0) : "주소 없음";
        } catch (Exception e) {
            return "주소 오류";
        }
    }

    private String convertWeather(String weather) {
        switch (weather) {
            case "Clear": return "맑음";
            case "Clouds": return "흐림";
            case "Rain": return "비";
            case "Drizzle": return "이슬비";
            case "Thunderstorm": return "천둥번개";
            case "Snow": return "눈";

            // 안개 계열
            case "Mist":
            case "Fog":
            case "Haze":
            case "Smoke":
                return "안개";

            // 먼지/모래 계열
            case "Dust":
            case "Sand":
            case "Ash":
                return "먼지";

            case "Squall": return "돌풍";
            case "Tornado": return "태풍";

            default: return weather;
        }
    }

    private void setWeatherIcon(String state) {
        switch (state) {
            case "맑음":
                ivWeatherIcon.setImageResource(R.drawable.sunny);
                break;
            case "흐림":
                ivWeatherIcon.setImageResource(R.drawable.overcast);
                break;
            case "비":
                ivWeatherIcon.setImageResource(R.drawable.rain);
                break;
            case "이슬비":
                ivWeatherIcon.setImageResource(R.drawable.lightrain);
                break;
            case "천둥번개":
                ivWeatherIcon.setImageResource(R.drawable.thunderstorm);
                break;
            case "눈":
                ivWeatherIcon.setImageResource(R.drawable.snow);
                break;
            case "안개":  // Mist, Fog, Haze, Smoke
                ivWeatherIcon.setImageResource(R.drawable.cloudy);
                break;
            case "먼지":  // Dust, Sand, Ash
                ivWeatherIcon.setImageResource(R.drawable.dust);
                break;
            case "돌풍":  // Squall
                ivWeatherIcon.setImageResource(R.drawable.windy);
                break;
            case "태풍":  // Tornado
                ivWeatherIcon.setImageResource(R.drawable.storm);
                break;
            default:
                ivWeatherIcon.setImageResource(R.drawable.clear);
                break;
        }
    }


    private void addPressAnimation(ImageView button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(120).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                    break;
            }
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("lat", selectedLat);
            selectedLon = data.getDoubleExtra("lon", selectedLon);
            selectedAddress = data.getStringExtra("address");

            tvLocation.setText(selectedAddress);
            requestWeather(selectedLat, selectedLon, selectedAddress);
        }
    }
}