package com.example.tripapp;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Trip implements Serializable {

    String tripId, tripName, ownerID;
    Timestamp startedAt, completedAt;
    double startLatitude, startLongitude, endLatitude, endLongitude, totalMiles;

    public Trip() {
    }

    public Trip(String tripId, String tripName, String ownerID, Timestamp startAt, Timestamp completeAt, double startLatitude, double startLongitude, double endLatitude, double endLongitude, double totalMiles) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.ownerID = ownerID;
        this.startedAt = startAt;
        this.completedAt = completeAt;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.totalMiles = totalMiles;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
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

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
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
