package com.example.weathertune.network.dto;

import java.util.List;

public class WeatherResponse {
    public Current current;
    public List<Hourly> hourly;

    public static class Current {
        public double temp;
        public long dt;
        public List<Weather> weather;
    }

    public static class Hourly {
        public long dt;
        public double pop;
    }

    public static class Weather {
        public String main;
        public String description;
    }
}