package com.example.tripapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener, CreateAccountFragment.CreateAccountListener, TripsFragment.TripsFragmentListener, CreateTripFragment.CreateTripListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.rootView, new LoginFragment())
                .commit();
    }




    @Override
    public void gotoTrips() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new TripsFragment()).addToBackStack(null)
                .commit();
    }

    @Override
    public void gotoCreateAccount() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new CreateAccountFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void login() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void goToNewTrip() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new CreateTripFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void doneCreatingTrip() {
        getSupportFragmentManager().popBackStack();
    }
}