package com.example.weathertracking;

import java.util.List;

public class GeocodingResponse {
    public List<GeoResult> results;

    public static class GeoResult {
        public String name;
        public double latitude;
        public double longitude;
    }
}
