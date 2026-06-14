package com.example.weathertracking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{
    private ArrayList<City> cities = new ArrayList<>();
    private OnCityClickListener listener;
    private boolean flag;
    private ViewHolder holderSave;

    public void setCities(ArrayList<City> cities){
        this.cities = cities;
        notifyDataSetChanged();
    }

    public interface OnCityClickListener{
        void onCitySelected(City city);
    }

    public void setOnClickListener(OnCityClickListener listener){ this.listener = listener; }

    public interface OnCityDoubleClickListener{
        void onCityDoubleClick(City city);
    }
    OnCityDoubleClickListener doubleClickListener;

    public void setOnDoubleClickListener(OnCityDoubleClickListener listener){
        this.doubleClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        City city = cities.get(position);
        holder.textView.setText(city.getTitle() + " " + city.getWeatherIndicator());

        long[] lastClickTime = {0};

        holder.textView.setOnClickListener(v ->{
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastClickTime[0] < 300) {
                if (doubleClickListener != null) doubleClickListener.onCityDoubleClick(city);
                lastClickTime[0] = 0;
            }
            else {
                lastClickTime[0] = currentTime;

                if (this.flag) {
                    holderSave.textView.setBackgroundColor(Color.rgb(150, 153, 146));
                    holderSave = holder;
                } else {
                    holderSave = holder;
                    this.flag = true;
                }

                holder.textView.setBackgroundColor(Color.rgb(0, 154, 99));
                if (listener != null) listener.onCitySelected(city);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewItem);
        }
    }
}

