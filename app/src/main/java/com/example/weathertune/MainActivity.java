package com.example.weathertune;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.weathertune.network.RetrofitClient;
import com.example.weathertune.network.WeatherApiService;
import com.example.weathertune.network.dto.WeatherResponse;
import com.example.weathertune.youtube.YouTubeApiService;
import com.example.weathertune.youtube.YouTubeResponse;
import com.example.weathertune.youtube.YouTubeRetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 1000;
    private static final int MAP_REQUEST_CODE = 200;
    private static final String WEATHER_API_KEY = BuildConfig.WEATHER_API_KEY;
    private static final String YOUTUBE_API_KEY = BuildConfig.YOUTUBE_API_KEY;

    private TextView tvLocation, tvTemperature, tvWeatherDescription, tvRainProbability, btnViewAll, tvWeatherAlert;;
    private ImageView btnSettings, ivWeatherIcon, refreshBtn, gpsBtn;
    private Button btnPlayNow;

    // Featured Music
    private TextView tvPlaylistTitle, tvPlaylistDescription;
    private String featuredVideoId = "";

    // Grid Layout 플레이리스트 카드들
    private GridLayout playlistGrid;

    FusedLocationProviderClient fusedLocationClient;

    double selectedLat = -1;
    double selectedLon = -1;
    String selectedAddress = null;
    private String currentWeatherKeyword = "";
    private List<YouTubeResponse.Item> currentYoutubeItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSettings = findViewById(R.id.btnSettings);
        tvLocation = findViewById(R.id.tvLocation);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherDescription = findViewById(R.id.tvWeatherDescription);
        tvRainProbability = findViewById(R.id.tvRainProbability);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        refreshBtn = findViewById(R.id.refresh_btn);
        gpsBtn = findViewById(R.id.gps_btn);
        btnPlayNow = findViewById(R.id.btnPlayNow);
        btnViewAll = findViewById(R.id.btnViewAll);
        btnPlayNow = findViewById(R.id.btnPlayNow);
        tvWeatherAlert = findViewById(R.id.tvWeatherAlert);

        tvPlaylistTitle = findViewById(R.id.tvPlaylistTitle);
        tvPlaylistDescription = findViewById(R.id.tvPlaylistDescription);
        playlistGrid = findViewById(R.id.playlistGrid);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        addPressAnimation(refreshBtn);
        addPressAnimation(gpsBtn);

        // 앱 첫 실행 → 초기 위치를 명지대로 강제 설정
        setInitialLocationToMyongji();

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnPlayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PlaylistActivity로 이동
                Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
                startActivity(intent);
            }
        });

        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllPlaylistsActivity.class);

            intent.putExtra("weatherKeyword", currentWeatherKeyword);

            // ★ YouTube 목록 전달 (JSON)
            if (currentYoutubeItems != null) {
                String json = new Gson().toJson(currentYoutubeItems);
                intent.putExtra("youtubeItemsJson", json);
            }

            startActivity(intent);
        });

        // Featured Music 재생 버튼
        btnPlayNow.setOnClickListener(v -> {
            if (!featuredVideoId.isEmpty()) {
                String url = "https://www.youtube.com/watch?v=" + featuredVideoId;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "음악을 불러오는 중입니다...", Toast.LENGTH_SHORT).show();
            }
        });

        refreshBtn.setOnClickListener(v -> {
            refreshBtn.animate().rotationBy(360f).setDuration(800).start();
            getWeather();
        });

        gpsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);

            // ★ 현재 선택된 위치를 그대로 넘긴다
            intent.putExtra("lat", selectedLat);
            intent.putExtra("lon", selectedLon);
            intent.putExtra("address", selectedAddress);

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
            if (location == null) {
                tvLocation.setText("위치 탐색 중...");
                return;
            }

            double lat = location.getLatitude();
            double lon = location.getLongitude();
            selectedAddress = getAddress(lat, lon);

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
                WEATHER_API_KEY
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

                // 자연스러운 한국어 변환
                String koreanWeather = convertWeather(main);
                String fixedDesc = convertDescriptionNatural(desc);

                tvWeatherDescription.setText(desc);

                // 날씨 키워드 저장
                saveWeatherKeyword(desc);

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
                tvTemperature.setText((int) temp + " °");
                tvWeatherDescription.setText(fixedDesc);
                tvRainProbability.setText((int) pop + "%");

                setWeatherIcon(koreanWeather);

                updateWeatherAlert(fixedDesc, (int) pop);

                // ★★★ 날씨에 맞는 음악 검색 ★★★
                searchYouTubeMusicByWeather(desc);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvWeatherDescription.setText("네트워크 오류");
            }
        });
    }

    // ★★★ YouTube 음악 검색 ★★★
    private void searchYouTubeMusicByWeather(String weatherDesc) {
        String searchQuery = getSearchQueryByWeather(weatherDesc);

        YouTubeApiService youtubeApi = YouTubeRetrofitClient
                .getInstance()
                .create(YouTubeApiService.class);

        Call<YouTubeResponse> call = youtubeApi.searchVideos(
                "snippet",
                searchQuery,
                "video",
                10,
                "KR",           // 한국 지역
                "ko",           // 한국어 필터
                YOUTUBE_API_KEY
        );

        call.enqueue(new Callback<YouTubeResponse>() {
            @Override
            public void onResponse(Call<YouTubeResponse> call, Response<YouTubeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    YouTubeResponse youtubeResponse = response.body();

                    if (youtubeResponse.items != null && !youtubeResponse.items.isEmpty()) {
                        currentYoutubeItems = youtubeResponse.items;

                        // 첫 번째 영상 = Featured Music
                        YouTubeResponse.Item firstItem = youtubeResponse.items.get(0);
                        featuredVideoId = firstItem.id.videoId;
                        tvPlaylistTitle.setText(getWeatherMusicDescription(weatherDesc));
                        tvPlaylistDescription.setText(firstItem.snippet.title);

                        // 나머지 최대 6개를 GridLayout에 표시
                        updatePlaylistGrid(youtubeResponse.items);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "YouTube 검색 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<YouTubeResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "YouTube API 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // GridLayout에 YouTube 데이터 채우기
    private void updatePlaylistGrid(List<YouTubeResponse.Item> items) {
        // GridLayout의 6개 카드 찾기
        int maxCards = Math.min(6, playlistGrid.getChildCount());
        int startIndex = 1; // 첫 번째는 Featured로 사용했으니 1번부터

        for (int i = 0; i < maxCards && (startIndex + i) < items.size(); i++) {
            View cardView = playlistGrid.getChildAt(i);
            if (cardView instanceof CardView) {
                YouTubeResponse.Item item = items.get(startIndex + i);
                updatePlaylistCard((CardView) cardView, item, i + 1);  // 카드 번호 전달 (1-6)
            }
        }
    }

    // 각 플레이리스트 카드 업데이트
    private void updatePlaylistCard(CardView card, YouTubeResponse.Item item, int cardIndex) {
        // 카드 내부의 모든 TextView를 순차적으로 찾기
        TextView titleView = findNthTextView(card, 0);  // 첫 번째 TextView = 제목
        TextView infoView = findNthTextView(card, 1);   // 두 번째 TextView = 설명

        if (titleView != null) {
            String title = item.snippet.title;
            titleView.setText(title);
            // 2줄 제한 + ... 표시 설정
            titleView.setMaxLines(2);
            titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }

        if (infoView != null) {
            infoView.setText(item.snippet.channelTitle);
        }

        // ★ YouTube 썸네일 이미지 로드
        int thumbnailId = getThumbnailId(cardIndex);
        ImageView thumbnailView = card.findViewById(thumbnailId);

        if (thumbnailView != null && item.snippet.thumbnails != null) {
            Glide.with(this)
                    .load(item.snippet.thumbnails.medium.url)
                    .placeholder(R.drawable.album_cover_gradient)
                    .into(thumbnailView);
        }

        // 카드 클릭 시 YouTube로 이동
        final String videoId = item.id.videoId;
        card.setOnClickListener(v -> {
            String url = "https://www.youtube.com/watch?v=" + videoId;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    // 썸네일 ImageView ID 가져오기 (AllPlaylistsActivity와 동일)
    private int getThumbnailId(int index) {
        switch (index) {
            case 1: return R.id.imgThumbnail1;
            case 2: return R.id.imgThumbnail2;
            case 3: return R.id.imgThumbnail3;
            case 4: return R.id.imgThumbnail4;
            case 5: return R.id.imgThumbnail5;
            case 6: return R.id.imgThumbnail6;
            default: return R.id.imgThumbnail1;
        }
    }

    // TextView 찾기 헬퍼 - N번째 TextView 찾기
    private TextView findTextViewByText(View parent, String text) {
        // 이 메서드는 더 이상 사용하지 않음
        return null;
    }

    // N번째 TextView 찾기 헬퍼 메서드
    private TextView findNthTextView(View parent, int targetIndex) {
        int[] currentIndex = {0};
        return findNthTextViewRecursive(parent, targetIndex, currentIndex);
    }

    private TextView findNthTextViewRecursive(View view, int targetIndex, int[] currentIndex) {
        if (view instanceof TextView) {
            if (currentIndex[0] == targetIndex) {
                return (TextView) view;
            }
            currentIndex[0]++;
        }

        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                TextView result = findNthTextViewRecursive(group.getChildAt(i), targetIndex, currentIndex);
                if (result != null) return result;
            }
        }
        return null;
    }

    // 날씨에 맞는 설명 생성
    private String getWeatherMusicDescription(String weatherDesc) {
        if (weatherDesc.contains("맑음") || weatherDesc.contains("clear")) {
            return "맑은 오전 감성 팝";
        } else if (weatherDesc.contains("비") || weatherDesc.contains("rain")) {
            return "비 오는 날 재즈";
        } else if (weatherDesc.contains("눈") || weatherDesc.contains("snow")) {
            return "겨울 저녁 발라드";
        } else if (weatherDesc.contains("흐림") || weatherDesc.contains("cloud")) {
            return "흐린 날 로파이";
        } else if (weatherDesc.contains("천둥") || weatherDesc.contains("thunder")) {
            return "천둥 번개 록";
        } else {
            return "지금 이 순간의 음악";
        }
    }

    // 날씨에 맞는 검색어 생성
    private String getSearchQueryByWeather(String weatherDesc) {
        if (weatherDesc.contains("맑음") || weatherDesc.contains("clear")) {
            return "신나는 음악 playlist";
        } else if (weatherDesc.contains("비") || weatherDesc.contains("rain")) {
            return "비오는날 감성 음악";
        } else if (weatherDesc.contains("눈") || weatherDesc.contains("snow")) {
            return "겨울 음악 playlist";
        } else if (weatherDesc.contains("흐림") || weatherDesc.contains("cloud")) {
            return "잔잔한 음악 모음";
        } else if (weatherDesc.contains("천둥") || weatherDesc.contains("thunder")) {
            return "강렬한 음악 playlist";
        } else if (weatherDesc.contains("안개") || weatherDesc.contains("fog") || weatherDesc.contains("mist")) {
            return "분위기 있는 음악";
        } else {
            return "감성 음악 모음";
        }
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
                ivWeatherIcon.setImageResource(R.drawable.ic_sunny);
                break;
            case "흐림":
                ivWeatherIcon.setImageResource(R.drawable.ic_overcast);
                break;
            case "비":
                ivWeatherIcon.setImageResource(R.drawable.ic_rainy);
                break;
            case "이슬비":
                ivWeatherIcon.setImageResource(R.drawable.ic_lightrain);
                break;
            case "천둥번개":
                ivWeatherIcon.setImageResource(R.drawable.ic_thunderstorm);
                break;
            case "눈":
                ivWeatherIcon.setImageResource(R.drawable.ic_snow);
                break;
            case "안개":  // Mist, Fog, Haze, Smoke
                ivWeatherIcon.setImageResource(R.drawable.ic_cloudy);
                break;
            case "먼지":  // Dust, Sand, Ash
                ivWeatherIcon.setImageResource(R.drawable.ic_dust);
                break;
            case "돌풍":  // Squall
                ivWeatherIcon.setImageResource(R.drawable.ic_windy);
                break;
            case "태풍":  // Tornado
                ivWeatherIcon.setImageResource(R.drawable.ic_storm);
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
    // 날씨 키워드 저장
    private void saveWeatherKeyword(String keyword) {
        currentWeatherKeyword = keyword;
    }

    // 날씨 키워드 출력
    public String getWeatherKeyword() {
        return currentWeatherKeyword;
    }

    // 자연스러운 한국어 설명으로 변환
    private String convertDescriptionNatural(String desc) {
        if (desc == null) return "날씨 정보 없음";

        String d = desc.trim();

        // 맑음
        if (d.contains("맑음")) return "하늘이 맑아요";

        // 구름 (약간/많음/튼구름/조금/온흐린/구름이 낀)
        if (d.contains("구름") || d.contains("흐린 하늘") || d.contains("흐린") || d.contains("튼구름")) {
            if (d.contains("약간") || d.contains("조금") || d.contains("튼구름")) {
                return "구름이 조금 끼었어요";
            }
            if (d.contains("많음") || d.contains("짙은")) {
                return "구름이 많아 흐린 편이에요";
            }
            return "하늘이 흐려요";  // 기본값
        }

        // 비
        if (d.contains("비") || d.contains("rain")) {
            if (d.contains("약한")) return "가벼운 비가 내려요";
            if (d.contains("강한") || d.contains("폭우")) return "비가 많이 내리고 있어요";
            if (d.contains("소나기")) return "소나기가 지나가고 있어요";
            return "비가 오고 있어요";
        }

        // 눈
        if (d.contains("눈") || d.contains("snow")) {
            if (d.contains("약한")) return "가볍게 눈이 내려요";
            if (d.contains("강한")) return "눈이 많이 내리고 있어요";
            return "눈이 내리고 있어요";
        }

        // 진눈깨비
        if (d.contains("진눈깨비")) return "진눈깨비가 내려요";

        // 안개 / 박무 / 흐린 안개
        if (d.contains("안개") || d.contains("박무") || d.contains("mist") || d.contains("fog")) {
            if (d.contains("옅은")) return "옅은 안개가 있어요";
            if (d.contains("짙은")) return "안개가 짙어요";
            return "안개가 조금 끼었어요";
        }

        // 먼지/황사
        if (d.contains("먼지") || d.contains("황사")) return "공기가 탁한 날씨예요";

        // 천둥번개
        if (d.contains("천둥") || d.contains("번개") || d.contains("thunder")) {
            return "천둥번개가 치고 있어요 ⚡";
        }

        // 돌풍
        if (d.contains("돌풍")) return "돌풍이 강하게 불고 있어요";

        // 기본값 (어떤 것도 매칭되지 않은 경우)
        return d;
    }

    // 앱 첫 실행 시 초기 위치를 명지대 자연캠으로 설정
    private void setInitialLocationToMyongji() {
        // 이미 선택된 위치가 있으면(지도 사용 등) 건드리지 않음
        if (selectedLat != -1 && selectedLon != -1) return;

        // 명지대 자연캠퍼스 기본 좌표
        selectedLat = 37.2244;
        selectedLon = 127.1866;
        selectedAddress = "명지대학교 자연캠퍼스";

        // 초기 UI 세팅
        tvLocation.setText(selectedAddress);

        // 날씨 요청 시작
        requestWeather(selectedLat, selectedLon, selectedAddress);
    }

    private void updateWeatherAlert(String weatherDesc, int rainProb) {

        String message = "지금 이 순간, 기분 좋은 음악 어떠세요?";

        // 강수확률 70% 이상이 우선
        if (rainProb >= 70) {
            message = "🌧️ 비 올 가능성이 높아요! 잔잔한 발라드를 추천할까요?";
        }

        // 날씨 설명 분석
        if (weatherDesc.contains("맑아요")) {
            message = "☀️ 날씨가 맑아요! 감성 팝 플레이리스트 어떠세요?";

        } else if (weatherDesc.contains("비")) {
            message = "🌧️ 비가 오고 있어요! 분위기 있는 재즈를 추천할까요?";

        } else if (weatherDesc.contains("눈")) {
            message = "❄️ 눈이 내리고 있어요! 따뜻한 발라드 어때요?";

        } else if (weatherDesc.contains("흐려요") || weatherDesc.contains("구름")) {
            message = "☁️ 흐린 날이에요. 잔잔한 로파이를 들어볼까요?";

        } else if (weatherDesc.contains("천둥") || weatherDesc.contains("⚡")) {
            message = "⚡ 천둥번개가 치고 있어요! 강렬한 록도 괜찮죠?";
        }

        // UI 적용
        tvWeatherAlert.setText(message);

        // 배너 클릭 → AllPlaylistsActivity 이동
        View parentCard = (View) tvWeatherAlert.getParent();

        parentCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllPlaylistsActivity.class);

            // 현재 유튜브 목록 전달
            if (currentYoutubeItems != null) {
                String json = new Gson().toJson(currentYoutubeItems);
                intent.putExtra("youtubeItemsJson", json);
            }

            // weatherKeyword 넘길 필요 없음
            startActivity(intent);
        });
    }
}