package com.example.weathertracking;

import java.util.List;

public class WeatherResponse {
    public String timezone;
    public Hourly hourly;

    public static class Hourly {
        public List<Float> temperature_2m;
        public List<Float> relative_humidity_2m;
        public List<Float> pressure_msl;
        public List<Float> wind_speed_10m;
        public List<Integer> weather_code;
    }
}
