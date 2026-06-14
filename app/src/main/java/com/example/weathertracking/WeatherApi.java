package com.example.weathertracking;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("v1/forecast")
    Call<WeatherResponse> getHourlyWeather(
            @Query("latitude") double lat,
            @Query("longitude") double lon,
            @Query("hourly") String hourly,
            @Query("timezone") String timezone
    );
}
