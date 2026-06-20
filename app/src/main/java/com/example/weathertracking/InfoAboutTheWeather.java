package com.example.weathertracking;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InfoAboutTheWeather extends AppCompatActivity {

    TextView Temperature_now;
    TextView pressure;
    TextView humidity;
    TextView windSpeed;
    TextView weathercode;
    TextView Time;

    ImageButton buttonClose;

    TextView CityTitle;

    City city;

    LineChart lineChart;

    ArrayList<Entry> entries = new ArrayList<>();
    Intent intentBack;

    private RainView rainView;
    private ImageView weatherBackground;

    static final boolean DEBUG_FORCE_RAIN = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info_about_the_weather);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        rainView = findViewById(R.id.rainView);
        //weatherBackground = findViewById(R.id.weatherBackground);

        intentBack = new Intent(InfoAboutTheWeather.this, MainActivity.class);

        buttonClose.setOnClickListener(v ->{
            startActivity(intentBack);
        });
        Intent intent = getIntent();
        city = (City) intent.getSerializableExtra("city");
        CityTitle.setText(city.getTitle());

        hourlyWeather(city.lat, city.lon);
    }

    // ------------------------------------------

    private void applyWeather(String condition) {
        switch (condition) {
            case "🌧️ Rain":
            case "🌦️ Drizzle":
            case "⛈️ Thunderstorm":
                //weatherBackground.setImageResource(R.drawable.bg_rain);
                rainView.setVisibility(View.VISIBLE);
                rainView.startRain();
                break;

            case "Clear":
                //weatherBackground.setImageResource(R.drawable.bg_sunny);
                rainView.setVisibility(View.GONE);
                rainView.stopRain();
                break;

            case "Clouds":
                //weatherBackground.setImageResource(R.drawable.bg_cloudy);
                rainView.setVisibility(View.GONE);
                rainView.stopRain();
                break;

            default:
                rainView.setVisibility(View.GONE);
                rainView.stopRain();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        rainView.stopRain(); // останавливаем когда приложение свёрнуто
    }

    @Override
    protected void onResume() {
        super.onResume();
        // если погода была дождливой — возобновляем
        if (rainView.getVisibility() == View.VISIBLE) {
            rainView.startRain();
        }
    }

    // ------------------------------------------

    void hourlyWeather(double lat, double lon){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi api = retrofit.create(WeatherApi.class);

        Call<WeatherResponse> call =
                api.getHourlyWeather(
                        lat,
                        lon,
                        "temperature_2m,relative_humidity_2m,pressure_msl,wind_speed_10m,weather_code",
                        "auto"
                );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call,
                                   Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();

                    entries.clear();

                    for (int i = 0; i < Math.min(24, weather.hourly.temperature_2m.size()); i++) {
                        Float temp = weather.hourly.temperature_2m.get(i);
                        entries.add(new Entry(i, temp));
                    }

                    TimeZone tz = TimeZone.getTimeZone(weather.timezone);
                    Calendar calendar = Calendar.getInstance(tz);
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

                    setCurrentHourCity(weather);
                    float temp = weather.hourly.temperature_2m.get(currentHour);

                    int weathercode1 = weather.hourly.weather_code.get(currentHour);
                    city.setWeatherIndicator(getWeatherDescription(weathercode1));
                    intentBack.putExtra("getCity", city);

                    float humidity1  = weather.hourly.relative_humidity_2m.get(currentHour);
                    float pressure1  = weather.hourly.pressure_msl.get(currentHour);
                    float wind1      = weather.hourly.wind_speed_10m.get(currentHour);

                    runOnUiThread(() -> {
                        Temperature_now.setText(temp + "°C");
                        weathercode.setText(getWeatherDescription(weathercode1));
                        humidity.setText("humidity: " + humidity1 + "%");
                        pressure.setText("pressure: " + pressure1 + " hPa");
                        windSpeed.setText("wind speed: " + wind1 + "km/h");
                        applyWeather(DEBUG_FORCE_RAIN ? "🌧️ Rain" : getWeatherDescription(weathercode1));
                    });
                    Weather_Chart();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                t.printStackTrace();
                Log.d("MyTag", t.getMessage() + " ошибка!!!!");
            }
        });
    }

    void initViews(){
        Temperature_now = findViewById(R.id.the_temperature_now);
        CityTitle = findViewById(R.id.city_name);
        pressure = findViewById(R.id.pressure);
        weathercode = findViewById(R.id.weathercode);
        humidity = findViewById(R.id.humidity);
        windSpeed = findViewById(R.id.wind_speed);
        Time = findViewById(R.id.time);
        buttonClose = findViewById(R.id.imageButtonClose);
    }

    void setCurrentHourCity(WeatherResponse weather) {
        TimeZone tz = java.util.TimeZone.getTimeZone(weather.timezone);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(tz);

        String currentHour = sdf.format(new Date());

        runOnUiThread(() -> Time.setText(currentHour));
    }

    void Weather_Chart(){
        lineChart = findViewById(R.id.lineChart);

        LineDataSet dataSet = new LineDataSet(entries, "Temperature");

        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisRight().setTextColor(Color.WHITE);
        lineChart.getLegend().setTextColor(Color.WHITE);

        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setLineWidth(3f);

        dataSet.setCircleRadius(3f);
        dataSet.setCircleColor(Color.RED);

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);

        dataSet.setDrawValues(false);


        Description description = new Description();
        description.setText("Hourly weather");
        description.setTextColor(Color.WHITE);
        lineChart.setDescription(description);

        lineChart.animateX(250);

        lineChart.invalidate();
    }

    String getWeatherDescription(int code) {
        if (code == 0) return "☀️ Clear";
        if (code <= 3) return "🌤️ Partly cloudy";
        if (code <= 48) return "🌫️ Foggy";
        if (code <= 55) return "🌦️ Drizzle";
        if (code <= 65) return "🌧️ Rain";
        if (code <= 75) return "🌨️ Snow";
        if (code <= 82) return "🌧️ Showers";
        if (code == 95) return "⛈️ Thunderstorm";
        return "🌡️ Unknown";
    }
}