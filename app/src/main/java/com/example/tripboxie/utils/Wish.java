package com.example.tripboxie.utils;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wish_table")
public class Wish {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String address;
    private String country;
    private String uri;
    private double latitude;
    private double longitude;

    public Wish(String name, String address, String country, String uri, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.country = country;
        this.uri = uri;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // 空的 constructor
    public Wish() {}

    // Getters & Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCountry() { return country; }
    public String getUri() { return uri; }
    public double getLatitude(){ return latitude; }
    public double getLongitude(){ return longitude; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setCountry(String country) { this.country = country; }
    public void setUri(String uri) { this.uri = uri; }
    public void setLatitude(double latitude){ this.latitude = latitude; }
    public void setLongitude(double longitude){this.longitude = longitude; }
    public void setAll(String name, String address, String country, String uri, double latitude, double longitude){
        this.name = name;
        this.address = address;
        this.country = country;
        this.uri = uri;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
