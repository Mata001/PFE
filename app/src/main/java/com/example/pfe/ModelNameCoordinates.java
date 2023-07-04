package com.example.pfe;


public class ModelNameCoordinates {
    String name,coordinates;

    public ModelNameCoordinates(String name, String coordinates) {
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

    public ModelNameCoordinates() {
    }
}
