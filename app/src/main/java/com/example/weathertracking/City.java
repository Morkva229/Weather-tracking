package com.example.weathertracking;

import java.io.Serializable;
import java.util.ArrayList;

public class City implements Serializable {
    private String title;
    public double lat = -100000000000.0;
    public double lon = -100000000000.0;
    private String weatherIndicator;

    City(String title, double lat, double lon) { this.title = title; this.lat = lat; this.lon = lon; }
    String getTitle(){
        return this.title;
    }
    String getWeatherIndicator() { return this.weatherIndicator; }
    void setWeatherIndicator(String weatherIndicator) { this.weatherIndicator = weatherIndicator; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return title.equals(city.title);
    }
}
