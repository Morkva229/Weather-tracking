package com.example.weathertracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton buttonDetailedInformation;
    FloatingActionButton buttonRecommendation;

    RecyclerView rvCities;
    Adapter adapter;
    ArrayList<City> cities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();

        Intent intent = getIntent();
        City city = (City) intent.getSerializableExtra("city selected");

        cities.add(new City("New York"));
        cities.add(new City("Vilnius"));
        cities.add(new City("Kyiv"));
        cities.add(new City("Paris"));
        cities.add(new City("Warsaw"));
        cities.add(new City("London"));
        cities.add(new City("Dubai"));
        cities.add(new City("Rome"));


        adapter.setCities(cities);
        rvCities.setAdapter(adapter);

        buttonDetailedInformation.setOnClickListener(v ->{
            Intent intent1 = new Intent(MainActivity.this, InfoAboutTheWeather.class);
            intent1.putExtra("city", city);

            startActivity(intent1);
        });
//        buttonRecommendation.setOnClickListener(v ->{
//
//        });
    }
    void initViews(){
        rvCities = findViewById(R.id.citiesItem);
        adapter = new Adapter();
        buttonDetailedInformation = findViewById(R.id.buttonDetailedInformation);
        buttonRecommendation = findViewById(R.id.buttonRecommendation);
    }
}