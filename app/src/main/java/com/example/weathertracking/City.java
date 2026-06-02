package com.example.weathertracking;

import java.io.Serializable;
import java.util.ArrayList;

public class City implements Serializable {
    private String title;

    City(String title){
        this.title = title;
    }
    String getTitle(){
        return this.title;
    }
}
