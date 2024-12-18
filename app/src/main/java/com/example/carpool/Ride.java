package com.example.carpool;

public class Ride {
    private String driverId;
    private String driverName;
    private double cost;
    private float distance;
    private String passengerId;  // New field for passenger's ID
    private String passengerName; // Add passenger name
    private float passengerRating;
    public Ride() {
        // Default constructor required for Firestore
        this.driverId = "";
        this.driverName = "";
        this.cost = 0.0;
        this.distance = 0.0f;
        this.passengerId = "";
    }
    // Constructor
    public Ride(String driverId, String driverName, double cost, float distance, String passengerId, String passengerName, float passengerRating) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.cost = cost;
        this.distance = distance;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.passengerRating = passengerRating;
    }

    public Ride(String driverId, String driverName, double cost, float distance, String passengerId) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.cost = cost;
        this.distance = distance;
        this.passengerId = passengerId;


    }

    // Getters and Setters
    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public float getPassengerRating() {
        return passengerRating;
    }

    public void setPassengerRating(float passengerRating) {
        this.passengerRating = passengerRating;
    }
    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }


}



