package com.example.pfe;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class ModelTram {
    String name,coordinates;

    public ModelTram(String name, String coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public ModelTram() {
    }
}
