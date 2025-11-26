package com.example.weathertune;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker;

    private EditText searchField;
    private ImageView backButton;
    private Button selectButton;

    private LatLng selectedLatLng;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchField = findViewById(R.id.search_field);
        selectButton = findViewById(R.id.btn_select_location);
        backButton = findViewById(R.id.btn_back);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        backButton.setOnClickListener(v -> finish());

        // Enter 키 → 검색 실행
        searchField.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                searchExactLocation(searchField.getText().toString());
                return true;
            }
            return false;
        });

        // 위치 선택 버튼
        selectButton.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "먼저 위치를 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("lat", selectedLatLng.latitude);
            intent.putExtra("lon", selectedLatLng.longitude);
            intent.putExtra("address", selectedAddress);

            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng defaultPos = new LatLng(37.2253, 127.1885);
        selectedLatLng = defaultPos;
        selectedAddress = "명지대학교 자연캠퍼스";

        marker = mMap.addMarker(new MarkerOptions().position(defaultPos).title("선택 위치"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 15));

        // 지도 터치 → 마커 이동 & 주소 갱신
        mMap.setOnMapClickListener(point -> {
            marker.setPosition(point);
            selectedLatLng = point;
            selectedAddress = getAddress(point.latitude, point.longitude);
        });
    }

    // 검색 → 위치 변환 → 지도 이동
    private void searchExactLocation(String query) {
        if (query.isEmpty()) {
            Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.KOREAN);

        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);

            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Address a = addresses.get(0);
            LatLng latLng = new LatLng(a.getLatitude(), a.getLongitude());

            marker.setPosition(latLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

            selectedLatLng = latLng;
            selectedAddress = a.getAddressLine(0);

        } catch (IOException e) {
            Toast.makeText(this, "좌표 변환 오류", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAddress(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.KOREAN);
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);
            return (list != null && !list.isEmpty()) ? list.get(0).getAddressLine(0) : "주소 없음";
        } catch (Exception e) {
            return "주소 없음";
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
