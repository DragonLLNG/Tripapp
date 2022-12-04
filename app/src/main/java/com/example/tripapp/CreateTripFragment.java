package com.example.tripapp;

import static android.content.Context.LOCATION_SERVICE;
import static com.google.android.gms.location.LocationRequest.Builder.IMPLICIT_MIN_UPDATE_INTERVAL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.tripapp.databinding.FragmentCreateTripBinding;
import com.example.tripapp.databinding.FragmentTripsBinding;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.guieffect.qual.UI;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateTripFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateTripFragment extends Fragment {

    FragmentCreateTripBinding binding;
    FusedLocationProviderClient client;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateTripFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CreateTripFragment newInstance(String param1, String param2) {
        CreateTripFragment fragment = new CreateTripFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = LocationServices.getFusedLocationProviderClient(getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateTripBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    CreateTripListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (CreateTripListener) context;
    }

    interface CreateTripListener{
        void doneCreatingTrip();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Create Trip");
        binding.locationLoadingStatus.setText(" Loading...");
        binding.locationLoadingStatus.setTextColor(getResources().getColor(R.color.yellow));


        binding.buttonSubmitNewTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tripName = binding.editTextTripName.getText().toString();

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
                    CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
                            .setGranularity(Granularity.GRANULARITY_FINE)
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .setDurationMillis(5000)
                            .setMaxUpdateAgeMillis(0)
                            .build();

                    client.getCurrentLocation(currentLocationRequest, cancellationTokenSource.getToken()).addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()){
                                binding.locationLoadingStatus.setText(" Success");
                                binding.locationLoadingStatus.setTextColor(getResources().getColor(R.color.green));

                                Location location = task.getResult();
                                Log.d("LocationTag", "onComplete: " +location);

                                if(tripName.isEmpty()){
                                    Toast.makeText(getActivity(), "Please enter a Trip name", Toast.LENGTH_SHORT).show();
                                } else {

                                    HashMap<String, Object> tripData = new HashMap<>();
                                    tripData.put("tripName", tripName);
                                    tripData.put("startAt", FieldValue.serverTimestamp());
                                    tripData.put("ownerId", user.getUid());
                                    tripData.put("completeAt", 0);
                                    tripData.put("startLatitude", location.getLatitude());
                                    tripData.put("startLongitude", location.getLongitude());
                                    tripData.put("endLatitude",null);
                                    tripData.put("endLongitude",null);
                                    tripData.put("totalMiles",0);

                                    DocumentReference tripDocRef = FirebaseFirestore.getInstance().collection("Trips").document();
                                    tripData.put("tripId", tripDocRef.getId());

                                    tripDocRef.set(tripData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mListener.doneCreatingTrip();

                                            }
                                            else{
                                                Toast.makeText(getActivity(), "Error creating trip", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                                }




                            } else {
                                task.getException().printStackTrace();
                            }
                        }
                    });


                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showCustomDialog("Location Permission", "This app needs location permission to track your location",
                                "Allow", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        multiplePermissionLauncher.launch(new String[]{
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION});
                                    }
                                }, "Cancel", null);
                    } else {
                        multiplePermissionLauncher.launch(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION});
                    }
                }
            }
        });
    }





//                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
//                        == PackageManager.PERMISSION_GRANTED
//                        && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
//                        == PackageManager.PERMISSION_GRANTED) {
//                    // When permission is granted
//                    // Call method
//                    client.getLastLocation()
//                            .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
//                                @Override
//                                public void onSuccess(Location location) {
//                                    // Got last known location. In some rare situations this can be null.
//
//
//                                    if (location != null) {
//                                        binding.locationLoadingStatus.setText("Success");
//
//                                        binding.locationLoadingStatus.setTextColor(getResources().getColor(R.color.teal_200));
//
//                                        // Logic to handle location object
//                                        Log.d("Location", "onSuccess:" + location.toString());
//                                    }
//                                }
//                            });
//                }
//                else{
//                    requestPermissions(
//                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 100);
//                    Log.d("Location", "onClick: No permission");
//                }

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
                    CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
                    CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
                            .setGranularity(Granularity.GRANULARITY_FINE)
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .setDurationMillis(5000)
                            .setMaxUpdateAgeMillis(0)
                            .build();

                    client.getCurrentLocation(currentLocationRequest, cancellationTokenSource.getToken()).addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()){
                                binding.locationLoadingStatus.setText(" Success");
                                binding.locationLoadingStatus.setTextColor(getResources().getColor(R.color.green));

                                Location location = task.getResult();
                                Log.d("LocationTag", "onComplete: " +location);
                            } else {
                                task.getException().printStackTrace();
                            }
                        }
                    });
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




//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode, @NonNull String[] permissions,
//            @NonNull int[] grantResults)
//    {
//        super.onRequestPermissionsResult(
//                requestCode, permissions, grantResults);
//        // Check condition
//        if (requestCode == 100 && (grantResults.length > 0)
//                && (grantResults[0] + grantResults[1]
//                == PackageManager.PERMISSION_GRANTED)) {
//            // When permission are granted
//            // Call  method
//            getCurrentLocation();
//        }
//        else {
//            // When permission are denied
//            // Display toast
//            Toast
//                    .makeText(getActivity(),
//                            "Permission denied",
//                            Toast.LENGTH_SHORT)
//                    .show();
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private void getCurrentLocation() {
//        LocationManager locationManager
//                = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
//        // Check condition
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            // When location service is enabled
//            // Get last location
//            client.getLastLocation().addOnCompleteListener(
//                    new OnCompleteListener<Location>() {
//                        @RequiresApi(api = Build.VERSION_CODES.S)
//                        @Override
//                        public void onComplete(
//                                @NonNull Task<Location> task)
//                        {
//                            // Initialize location
//                            Location location = task.getResult();
//                            // Check condition
//                            if (location != null) {
//                                // When location result is not null
//                                Log.d("Location", "onComplete: "+location.toString());
//                                binding.locationLoadingStatus.setText(location.describeContents());
//                            }
//                            else {
//                                // When location result is null
//                                // initialize location request
//                                LocationRequest locationRequest = new LocationRequest.Builder(60 * 60 * 1000)
//                                        .setMinUpdateIntervalMillis(500)
//                                        .setMaxUpdateDelayMillis(1000)
//                                        .build();
//
//                                // Initialize location call back
//                                LocationCallback locationCallback = new LocationCallback() {
//                                    @Override
//                                    public void
//                                    onLocationResult(LocationResult locationResult)
//                                    {
//                                        // Initialize
//                                        // location
//                                        Location location1 = locationResult.getLastLocation();
//                                        Log.d("Location", "onComplete: "+location1.toString());
//
//                                    }
//                                };
//
//                                // Request location updates
//                               // client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
//                            }
//                        }
//                    });
//        }
//        else {
//            // When location service is not enabled
//            // open location setting
//            startActivity(
//                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//        }
//    }

}


