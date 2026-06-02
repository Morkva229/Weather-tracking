package com.example.weathertracking;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingApi {
    @GET("v1/search")
    Call<GeocodingResponse> getCoordinates(
            @Query("name") String cityName,
            @Query("count") int count,
            @Query("language") String language
    );
}