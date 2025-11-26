package com.example.weathertune.network;

import android.content.Context;

import com.example.weathertune.BuildConfig;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {

        if (retrofit == null) {

            // 📌 1) Logging Interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            // 📌 2) 디스크 캐시 (5MB)
            Cache cache = new Cache(
                    new File(context.getCacheDir(), "http_cache"),
                    5 * 1024 * 1024
            );

            // 📌 3) 캐시 정책 Interceptor
            Interceptor cacheInterceptor = chain -> {
                Request request = chain.request();

                // 네트워크 연결 상관없이 캐시 우선
                Response response = chain.proceed(request);

                return response.newBuilder()
                        .header("Cache-Control", "public, max-age=60") // 60초 캐시
                        .build();
            };

            // 📌 4) GZIP 강제 + TIMEOUT 단축 + HTTP/2 안정화
            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(logging)
                    .addInterceptor(cacheInterceptor)
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .header("Accept-Encoding", "gzip")
                                .build();
                        return chain.proceed(request);
                    })
                    .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();

            // 📌 5) Retrofit Build
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.openweathermap.org/") // Base URL
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
