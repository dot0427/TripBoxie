package com.example.tripboxie.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DayActivity implements Serializable {
    private String name;
    private String address;
    private String country;
    private String uri;
    private double latitude;
    private double longitude;
    private String startTime;
    private String endTime;
    private int orderIndex;
    private List<ToDoItem> todoList = new ArrayList<>();



    public DayActivity(String name, String address, String country, String uri, double latitude, double longitude, String startTime, String endTime, int orderIndex) {
        this.name = name;
        this.address = address;
        this.country = country;
        this.uri = uri;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startTime = startTime;
        this.endTime = endTime;
        this.orderIndex = orderIndex;
    }

    // Getters 和 Setters
    public String getName() {return name; }
    public String getAddress() { return address; }
    public String getCountry() { return country; }
    public String getUri() { return uri; }
    public double getLatitude(){ return latitude; }
    public double getLongitude(){ return longitude; }
    public String getStartTime(){ return startTime; }
    public String getEndTime(){ return endTime; }
    public int getOrderIndex(){ return orderIndex; }
    public List<ToDoItem> getTodoList() { return todoList; }

    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setCountry(String country) { this.country = country; }
    public void setUri(String uri) { this.uri = uri; }
    public void setLatitude(double latitude){ this.latitude = latitude; }
    public void setLongitude(double longitude){this.longitude = longitude; }
    public void setStartTime(String startTime){ this.startTime = startTime; }
    public void setEndTime(String endTime){ this.endTime = endTime; }
    public void setOrderIndex(int orderIndex){ this.orderIndex = orderIndex; }
    public void setTodoList(List<ToDoItem> todoList) {
        this.todoList = todoList;
    }
    public void setAll(String name, String address, String country, String uri, double latitude, double longitude, String startTime, String endTime, int orderIndex) {
        this.name = name;
        this.address = address;
        this.country = country;
        this.uri = uri;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startTime = startTime;
        this.endTime = endTime;
        this.orderIndex = orderIndex;
    }

}
