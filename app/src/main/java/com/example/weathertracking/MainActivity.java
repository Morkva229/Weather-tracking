package com.example.weathertracking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.Entry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton buttonDetailedInformation;
    AutoCompleteTextView autoCompleteTextView;

    RecyclerView rvCities;
    Adapter adapter;
    ArrayList<City> cities = new ArrayList<>();
    City selectedCity = null;

    Gson gson;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    Handler searchHandler = new Handler(Looper.getMainLooper());
    Runnable searchRunnable;
    Retrofit retrofit;
    GeocodingApi geoApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gson = new Gson();
        prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);
        editor = prefs.edit();

        initViews();

        loadCities();

        Intent intent = getIntent();
         City newCity = (City) intent.getSerializableExtra("getCity");
         if (newCity != null && !cities.contains(newCity)){
             cities.add(newCity);
             adapter.setCities(cities);
             saveCities(cities);
         }

        adapter.setCities(cities);
        rvCities.setAdapter(adapter);

        adapter.setOnClickListener(city -> {
            selectedCity = city;
        });

        adapter.setOnDoubleClickListener(city -> {
            cities.remove(city);
            adapter.setCities(cities);
            saveCities(cities);
        });

        buttonDetailedInformation.setOnClickListener(v -> {
            if (selectedCity == null || !cities.contains(selectedCity)) {
                String textCity = autoCompleteTextView.getText().toString().trim();
                if (textCity.isEmpty()) {
                    Toast.makeText(this, "first choose a city!", Toast.LENGTH_SHORT).show();
                    return;
                }

                findCity(textCity, new OnCityFoundListener() {
                    @Override
                    public void onFound(double lat, double lon) {
                        City city = new City(textCity, lat, lon);

                        runOnUiThread(() ->{
                            Intent intent1 = new Intent(MainActivity.this, InfoAboutTheWeather.class);
                            intent1.putExtra("city", city);
                            startActivity(intent1);
                        });

                    }

                    @Override
                    public void onNotFound() {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "City not found!", Toast.LENGTH_SHORT).show()
                        );
                    }
                });

            } else {
                saveCities(cities);
                Intent intent1 = new Intent(MainActivity.this, InfoAboutTheWeather.class);
                intent1.putExtra("city", selectedCity);
                startActivity(intent1);
            }
        });
    }
    void initViews(){
        rvCities = findViewById(R.id.citiesItem);
        adapter = new Adapter();
        buttonDetailedInformation = findViewById(R.id.buttonDetailedInformation);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://geocoding-api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geoApi = retrofit.create(GeocodingApi.class);

        setupCityAutocomplete();
    }

    void setupCityAutocomplete() {
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);

                if (s.length() < 2) return;

                searchRunnable = () -> {
                    geoApi.getCoordinates(s.toString(), 5, "en")
                            .enqueue(new Callback<GeocodingResponse>() {
                                @Override
                                public void onResponse(Call<GeocodingResponse> call,
                                                       Response<GeocodingResponse> response) {
                                    if (response.isSuccessful()
                                            && response.body() != null
                                            && response.body().results != null) {

                                        List<String> cityNames = new ArrayList<>();
                                        for (GeocodingResponse.GeoResult r : response.body().results) {
                                            cityNames.add(r.name + ", " + r.country);
                                        }

                                        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(
                                                MainActivity.this,
                                                android.R.layout.simple_dropdown_item_1line,
                                                cityNames);
                                        autoCompleteTextView.setAdapter(dropdownAdapter);
                                        dropdownAdapter.notifyDataSetChanged();
                                        autoCompleteTextView.showDropDown();
                                    }
                                }

                                @Override
                                public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                                    Log.e("Autocomplete", "Error: " + t.getMessage());
                                }
                            });
                };
                searchHandler.postDelayed(searchRunnable, 400);
            }
        });

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            String cityOnly = selected.split(",")[0].trim();
            autoCompleteTextView.setText(cityOnly);
            autoCompleteTextView.setSelection(cityOnly.length());
        });
    }

    interface OnCityFoundListener {
        void onFound(double lat, double lon);
        void onNotFound();
    }

    void findCity(String cityName, OnCityFoundListener listener) {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://geocoding-api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geoApi = retrofit.create(GeocodingApi.class);
        WeatherApi weatherApi = retrofit.create(WeatherApi.class);

        geoApi.getCoordinates(cityName, 1, "en").enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                if (response.isSuccessful() && response.body() != null
                        && response.body().results != null) {
                    GeocodingResponse.GeoResult result = response.body().results.get(0);

                    Call<WeatherResponse> call1 =
                            weatherApi.getHourlyWeather(
                                    result.latitude,
                                    result.longitude,
                                    "weather_code",
                                    "auto"
                            );

                    listener.onFound(result.latitude, result.longitude);
                } else {
                    listener.onNotFound();
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                listener.onNotFound();
            }
        });
    }

    void saveCities(ArrayList<City> cities){
        String json = gson.toJson(cities);

        editor.putString("cities", json);
        editor.apply();
    }

    void loadCities(){
        String json = prefs.getString("cities", null);
        if (json != null){
            Type type = new TypeToken<ArrayList<City>>(){}.getType();
            cities = gson.fromJson(json, type);
        }
    }
}