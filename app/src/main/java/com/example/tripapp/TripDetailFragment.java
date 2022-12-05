package com.example.tripapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tripapp.databinding.FragmentTripDetailBinding;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class TripDetailFragment extends Fragment {

    Trip mTrip;
    FusedLocationProviderClient client;
    GoogleMap mMap;
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

    private static final String ARG_PARAM_TRIP = "param_trip";

    public TripDetailFragment() {
        // Required empty public constructor
    }


    public static TripDetailFragment newInstance(Trip data) {
        TripDetailFragment fragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_TRIP, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

      client = LocationServices.getFusedLocationProviderClient(getContext());
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrip = (Trip) getArguments().getSerializable(ARG_PARAM_TRIP);
        }

    }



    FragmentTripDetailBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {



        binding = FragmentTripDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.googleMap);

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {

                mMap = googleMap;

                UiSettings mUiSettings;

                    mUiSettings = googleMap.getUiSettings();

                    mUiSettings.setZoomControlsEnabled(true);
                    LatLng start = new LatLng(mTrip.startLatitude,mTrip.startLongitude);
                    googleMap.addMarker(new MarkerOptions().position(start).title("Marker in Start"));
                    if (mTrip.completedAt!=null) {
                        LatLng end = new LatLng(mTrip.endLatitude, mTrip.endLongitude);
                        googleMap.addMarker(new MarkerOptions().position(end).title("Marker in End"));


                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(start);
                        builder.include(end);

                        int width = getResources().getDisplayMetrics().widthPixels;
                        int height = getResources().getDisplayMetrics().heightPixels;
                        int padding = (int) (width * 0.20); // offset from edges of the map 10% of screen

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));

                    }
                    else {

                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(start));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 12));
                    }
                }

        });



        binding.textViewTrip.setText(mTrip.tripName);
        binding.textViewStart.setText("Started At: "+sdf.format(mTrip.startedAt.toDate()));

        if(mTrip.completedAt ==null){
            binding.textViewComplete.setText("Completed At: N/A");
            binding.textViewStatus.setText("On Going");
            binding.textViewStatus.setTextColor(getResources().getColor(R.color.orange));
            binding.textViewMiles.setVisibility(View.INVISIBLE);
            binding.btnComplete.setVisibility(View.VISIBLE);

            binding.btnComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setUpLocationUpdates();
                }
            });
        }
        else {
            binding.textViewComplete.setText("Completed At: "+sdf.format(mTrip.completedAt.toDate()));
            binding.textViewStatus.setText("Completed");
            binding.textViewStatus.setTextColor(getResources().getColor(R.color.green));
            binding.textViewMiles.setVisibility(View.VISIBLE);
            binding.btnComplete.setVisibility(View.INVISIBLE);
            binding.textViewMiles.setText(String.format("%.2f", mTrip.totalMiles)+" Miles");
        }


    }

    private final static int REQUEST_CHECK_CODE = 1001;
    private void setUpLocationUpdates(){
        com.google.android.gms.location.LocationRequest locationRequest = new com.google.android.gms.location.LocationRequest.Builder(5000)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(100)
                .build();

        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                if(task.isSuccessful()){
                    client.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
                } else {
                    if(task.getException() instanceof ResolvableApiException){
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) task.getException();
                            resolvableApiException.startResolutionForResult(getActivity(),REQUEST_CHECK_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            }
        });

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d("demo", "onLocationResult: "+locationResult);


            DocumentReference tripRef = FirebaseFirestore.getInstance().collection("Trips").document(mTrip.tripId);

            tripRef.update("completedAt", FieldValue.serverTimestamp());
            tripRef.update("endLatitude",locationResult.getLocations().get(0).getLatitude());
            tripRef.update("endLongitude",locationResult.getLocations().get(0).getLongitude());

            float[] results = new float[1];
            Location.distanceBetween(mTrip.startLatitude, mTrip.startLongitude,
                    locationResult.getLocations().get(0).getLatitude(), locationResult.getLocations().get(0).getLongitude(), results);
            double distance = results[0];
            double miles = distance*0.000621371192;



            tripRef.update("totalMiles",miles);

            binding.textViewComplete.setText("Completed At: "+sdf.format(new Date()));
            binding.textViewStatus.setText("Completed");
            binding.textViewStatus.setTextColor(getResources().getColor(R.color.green));
            binding.textViewMiles.setVisibility(View.VISIBLE);
            binding.btnComplete.setVisibility(View.INVISIBLE);
            binding.textViewMiles.setText(String.format("%.2f", miles)+" Miles");


            LatLng start = new LatLng(mTrip.startLatitude,mTrip.startLongitude);
            mMap.addMarker(new MarkerOptions().position(start).title("Marker in Start"));
            LatLng end = new LatLng(locationResult.getLocations().get(0).getLatitude()
                    , locationResult.getLocations().get(0).getLongitude());
            mMap.addMarker(new MarkerOptions().position(end).title("Marker in End"));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(start);
            builder.include(end);

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.20);

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));


        }
    };

}

