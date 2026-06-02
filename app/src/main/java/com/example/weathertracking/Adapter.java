package com.example.weathertracking;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{
    private List<City> cities = new ArrayList<>();

    public void setCities(List<City> cities){
        this.cities = cities;
        notifyDataSetChanged();
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
        holder.textView.setText(city.getTitle());

        holder.textView.setOnClickListener(v ->{
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            holder.textView.setBackgroundColor(Color.GREEN);
            
            intent.putExtra("city selected", city);
            Toast.makeText(v.getContext(), "Currently selected: " + city.getTitle(), Toast.LENGTH_SHORT).show();
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

