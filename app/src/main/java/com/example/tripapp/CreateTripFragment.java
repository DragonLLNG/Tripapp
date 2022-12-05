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

                                } else if( binding.locationLoadingStatus.getText().toString()==" Loading..."){
                                    Toast.makeText(getActivity(), "Still loading", Toast.LENGTH_SHORT).show();
                                } else {

                                    HashMap<String, Object> tripData = new HashMap<>();
                                    tripData.put("tripName", tripName);
                                    tripData.put("startedAt", FieldValue.serverTimestamp());
                                    tripData.put("ownerId", user.getUid());
                                    tripData.put("completedAt", null);
                                    tripData.put("startLatitude", location.getLatitude());
                                    tripData.put("startLongitude", location.getLongitude());
                                    tripData.put("endLatitude",0.0);
                                    tripData.put("endLongitude",0.0);
                                    tripData.put("totalMiles",0.0);

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


}


