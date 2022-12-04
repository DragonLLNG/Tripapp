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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Map;

public class TripDetailFragment extends Fragment {

    Trip mTrip;
    FusedLocationProviderClient client;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();




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

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.map_container, mapFragment)
                .commit();

    }



    FragmentTripDetailBinding binding;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng current = new LatLng(mTrip.startLatitude,mTrip.startLongitude);
            googleMap.addMarker(new MarkerOptions().position(current).title("Marker in Start"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        }
    };

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
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");


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


                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        setUpLocationUpdates();

                    }




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



        }
    };

    void showCustomDialog(String title, String message,
                          String positiveBntTitle, DialogInterface.OnClickListener positiveListener,
                          String negativeBntTitle, DialogInterface.OnClickListener negativeListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveBntTitle,positiveListener)
                .setNegativeButton(negativeBntTitle,negativeListener);
        builder.create().show();
    }
    private ActivityResultLauncher<String[]> multiplePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @SuppressLint("MissingPermission")
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            boolean fineLocationAllowed = false;
            if(result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null) {
                fineLocationAllowed = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                if (fineLocationAllowed) {

                    setUpLocationUpdates();


                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showCustomDialog("Location Permission", "This app needs the fine location permission to track your location",
                                "Allow", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Uri.parse("package: " + BuildConfig.APPLICATION_ID));
                                        startActivity(intent);
                                    }
                                }, "Cancel", null);
                    }

                }
            }
        }
    });
}

