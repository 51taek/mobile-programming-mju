package com.example.weathertune;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.weathertune.youtube.YouTubeResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AllPlaylistsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private GridLayout playlistGrid;
    private TextView tvSectionTitle;

    private List<YouTubeResponse.Item> items; // MainActivity에서 받은 유튜브 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_playlists);

        btnBack = findViewById(R.id.btnBack);
        playlistGrid = findViewById(R.id.play_all_list_grid);
        tvSectionTitle = findViewById(R.id.section_weather_title);

        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());

        // 타이틀 (날씨 키워드)
        String weatherKeyword = getIntent().getStringExtra("weatherKeyword");
        if (weatherKeyword == null) weatherKeyword = "분위기 음악";

        tvSectionTitle.setText(convertDescriptionNatural(weatherKeyword));

        // ★★★ MainActivity에서 전달된 JSON 파싱 ★★★
        String json = getIntent().getStringExtra("youtubeItemsJson");
        if (json != null) {
            Type listType = new TypeToken<List<YouTubeResponse.Item>>() {}.getType();
            items = new Gson().fromJson(json, listType);
        }

        // 데이터 없으면 종료
        if (items == null || items.isEmpty()) return;

        // ★ Grid 업데이트
        updateGrid(items);
    }

    // GridLayout 카드 업데이트
    private void updateGrid(List<YouTubeResponse.Item> items) {

        int maxCards = Math.min(playlistGrid.getChildCount(), items.size());

        for (int i = 0; i < maxCards; i++) {
            View cardView = playlistGrid.getChildAt(i);

            if (cardView instanceof CardView) {
                updatePlaylistCard((CardView) cardView, items.get(i), i+1);
            }
        }
    }

    // 개별 카드 설정
    private void updatePlaylistCard(CardView card, YouTubeResponse.Item item, int index) {

        TextView titleView = findTextInside(card, 0);
        TextView infoView = findTextInside(card, 1);

        if (titleView != null) titleView.setText(item.snippet.title);
        if (infoView != null) infoView.setText(item.snippet.channelTitle);

        int thumbnailId = getThumbnailId(index);
        ImageView thumbnailView = card.findViewById(thumbnailId);

        if (thumbnailView != null && item.snippet.thumbnails != null) {
            Glide.with(this)
                    .load(item.snippet.thumbnails.medium.url)
                    .placeholder(R.drawable.album_cover_gradient)
                    .into(thumbnailView);
        }

        card.setOnClickListener(v -> {
            String url = "https://www.youtube.com/watch?v=" + item.id.videoId;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });
    }

    private int getThumbnailId(int index) {
        switch (index) {
            case 1: return R.id.imgThumbnail1;
            case 2: return R.id.imgThumbnail2;
            case 3: return R.id.imgThumbnail3;
            case 4: return R.id.imgThumbnail4;
            case 5: return R.id.imgThumbnail5;
            case 6: return R.id.imgThumbnail6;
            case 7: return R.id.imgThumbnail7;
            case 8: return R.id.imgThumbnail8;
            case 9: return R.id.imgThumbnail9;
            case 10: return R.id.imgThumbnail10;
        }
        return R.id.imgThumbnail1;
    }


    // Card 내부 TextView 찾기
    private TextView findTextInside(View parent, int index) {
        return findNthTextView(parent, index, new int[]{0});
    }

    private TextView findNthTextView(View view, int targetIndex, int[] currentIndex) {
        if (view instanceof TextView) {
            if (currentIndex[0] == targetIndex) {
                return (TextView) view;
            }
            currentIndex[0]++;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                TextView result = findNthTextView(group.getChildAt(i), targetIndex, currentIndex);
                if (result != null) return result;
            }
        }
        return null;
    }

    // 자연스러운 한국어 설명으로 변환
    private String convertDescriptionNatural(String desc) {
        if (desc == null) return "날씨 정보 없음";

        String d = desc.trim();

        // 맑음
        if (d.contains("맑음")) return "하늘이 맑아요";

        // 구름
        if (d.contains("구름 조금") || d.contains("튼구름")) return "구름이 조금 끼었어요";
        if (d.contains("구름 많음")) return "구름이 많아 흐린 편이에요";
        if (d.contains("흐림")) return "하늘이 흐려요";

        // 비
        if (d.contains("약한 비")) return "가벼운 비가 내려요";
        if (d.contains("비")) return "비가 오고 있어요";
        if (d.contains("강한 비") || d.contains("폭우")) return "비가 많이 내리고 있어요";

        // 소나기
        if (d.contains("소나기")) return "소나기가 지나가고 있어요";

        // 눈
        if (d.contains("약한 눈")) return "가볍게 눈이 내려요";
        if (d.contains("눈")) return "눈이 내리고 있어요";
        if (d.contains("강한 눈")) return "눈이 많이 내리고 있어요";

        // 진눈깨비
        if (d.contains("진눈깨비")) return "진눈깨비가 내려요";

        // 안개
        if (d.contains("안개")) return "안개가 조금 끼었어요";
        if (d.contains("옅은 안개")) return "옅은 안개가 있어요";
        if (d.contains("짙은 안개")) return "안개가 짙어요";

        // 미세먼지/황사
        if (d.contains("먼지")) return "먼지가 많아 공기가 탁해요";
        if (d.contains("황사")) return "황사가 있어 공기가 좋지 않아요";

        // 천둥/번개
        if (d.contains("천둥") || d.contains("번개")) return "천둥번개가 치고 있어요 ⚡";

        // 돌풍
        if (d.contains("돌풍")) return "돌풍이 강하게 불고 있어요";

        // 기본: 변환 못하면 원본 그대로 출력
        return d;
    }
}