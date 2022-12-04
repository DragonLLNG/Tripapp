package com.example.tripapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tripapp.databinding.FragmentTripsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TripsFragment extends Fragment {

    FragmentTripsBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ListenerRegistration tripRegistration;
    ArrayList<Trip> mTrips = new ArrayList<>();
    tripsAdapter adapter;

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

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new tripsAdapter(mTrips);
        binding.recyclerView.setAdapter(adapter);


        tripRegistration = FirebaseFirestore.getInstance().collection("Trips")
//                .whereArrayContains("ownerId", mAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        Log.d("demo", "onEvent: addSnapshotListener inside MyChatsFragment");
                        mTrips.clear();
                        for (QueryDocumentSnapshot doc: value) {
                            Trip trip = doc.toObject(Trip.class);
                            mTrips.add(trip);
                            adapter.notifyDataSetChanged();
                            Log.d("demo", "onEvent: "+trip.tripName);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });




    }

    @Override
    public void onPause() {
        super.onPause();
        if(tripRegistration != null){
            tripRegistration.remove();
        }
    }


    class tripsAdapter extends RecyclerView.Adapter<tripsAdapter.TripsViewHolder>{
        ArrayList<Trip> mTrips = new ArrayList<>();
        public tripsAdapter(ArrayList<Trip> data){
            this.mTrips = data;
        }

        @NonNull
        @Override
        public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trips_list_item, parent, false);
            tripsAdapter.TripsViewHolder tripsViewHolder = new TripsViewHolder(view);

            return tripsViewHolder;

        }

        @Override
        public void onBindViewHolder(@NonNull TripsViewHolder holder, int position) {
            Trip trip = mTrips.get(position);
            Log.d("test", "onBindViewHolder: "+ trip.tripName);

            holder.tripName.setText(trip.tripName);


            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
            holder.startedAt.setText("Started At: "+sdf.format(trip.startAt.toDate()));

            if(trip.completeAt==null){
                holder.completedAt.setText("Completed At: N/A");
                holder.status.setText("On Going");
                holder.status.setTextColor(getResources().getColor(R.color.orange));
                holder.miles.setText("");
            }
            else {
                holder.completedAt.setText("Completed At: "+sdf.format(trip.completeAt.toDate()));
                holder.status.setText("Completed");
                holder.status.setTextColor(getResources().getColor(R.color.green));
                holder.miles.setText(trip.totalMiles+" Miles");
            }
        }

        @Override
        public int getItemCount() {
            return this.mTrips.size();
        }

        public class TripsViewHolder extends RecyclerView.ViewHolder {

            TextView tripName, startedAt, completedAt, status, miles;
            Trip mTrip;

            public TripsViewHolder(@NonNull View itemView) {
                super(itemView);

                tripName = itemView.findViewById(R.id.textViewTrip);
                startedAt = itemView.findViewById(R.id.textViewStart);
                completedAt = itemView.findViewById(R.id.textViewComplete);
                status = itemView.findViewById(R.id.textViewStatus);
                miles = itemView.findViewById(R.id.textViewMiles);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.goToTripDetails();

                    }
                });

            }
        }

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
        void goToTripDetails();
    }
}