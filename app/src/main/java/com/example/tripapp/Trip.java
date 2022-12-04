package com.example.tripapp;

import com.google.firebase.Timestamp;

public class Trip {

    String tripName, ownerID;
    Timestamp startAt, completeAt;
    double startLatitude, startLongitude, endLatitude, endLongitude, totalMiles;

    public Trip() {
    }

    public Trip(String tripName, String ownerID, Timestamp startAt, Timestamp completeAt, double startLatitude, double startLongitude, double endLatitude, double endLongitude, double totalMiles) {
        this.tripName = tripName;
        this.ownerID = ownerID;
        this.startAt = startAt;
        this.completeAt = completeAt;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.totalMiles = totalMiles;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public Timestamp getStartAt() {
        return startAt;
    }

    public void setStartAt(Timestamp startAt) {
        this.startAt = startAt;
    }

    public Timestamp getCompleteAt() {
        return completeAt;
    }

    public void setCompleteAt(Timestamp completeAt) {
        this.completeAt = completeAt;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public double getTotalMiles() {
        return totalMiles;
    }

    public void setTotalMiles(double totalMiles) {
        this.totalMiles = totalMiles;
    }

}
