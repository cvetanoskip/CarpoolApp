package com.example.carpool;

import com.google.firebase.firestore.PropertyName;

public class Driver {
    private String Name;  // Change 'name' to 'Name'
    private String id;
    private String role;
    private float rating;

    private String Cost;;
    private String Vehicle;  // New field for Vehicle
    private String workingHours;



    // Constructor (needed for Firebase Firestore)
    public Driver() {}

    // Getters and Setters
    public String getName() {
        return Name;  // Make sure this matches the Firestore field
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getPricePerKm() {

            return Cost;

    }

    public void setPricePerKm(String  pricePerKm) {
        this.Cost = pricePerKm;
    }
    public String getVehicle() {
        return Vehicle;  // Getter for Vehicle
    }

    public void setVehicle(String vehicle) {
        this.Vehicle = vehicle;  // Setter for Vehicle
    }

    public String getWorkingHours() {
        return workingHours;  // Getter for Working hours
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;  // Setter for Working hours
    }
}

