package com.example.tripapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tripapp.databinding.FragmentTripsBinding;

public class TripsFragment extends Fragment {

    FragmentTripsBinding binding;



    public TripsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTripsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Trips");

        binding.buttonNewTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goToNewTrip();
            }
        });
    }


    TripsFragmentListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof TripsFragmentListener) {
            mListener = (TripsFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement forumsFragmentListener");
        }
    }

    public interface TripsFragmentListener {
        void goToNewTrip();
    }
}