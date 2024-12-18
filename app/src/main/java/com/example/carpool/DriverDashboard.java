package com.example.carpool;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriverDashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverDashboard extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private EditText workingHoursInput;
    private EditText currentWorkingHours;
    private Button saveButton;
    private FirebaseFirestore db;
    FirebaseAuth auth;
    private TextView rating,name;
    private FirebaseUser user;
    private RecyclerView driverHistoryRecyclerView;
    private DriverHistoryAdapter adapter;
    private List<Ride> rideList;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DriverDashboard() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DriverDashboard.
     */
    // TODO: Rename and change types and number of parameters
    public static DriverDashboard newInstance(String param1, String param2) {
        DriverDashboard fragment = new DriverDashboard();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_dashboard, container, false);
        rating=view.findViewById(R.id.textView3);
        name=view.findViewById(R.id.nametext);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        workingHoursInput = view.findViewById(R.id.workingHoursInput);
        db.collection("user")
                .document(user.getUid())  // Get the current user's document
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the user's name and rating from Firestore
                        String driverName = documentSnapshot.getString("Name");  // Assuming "Name" field exists
                        Double ratingValue = documentSnapshot.getDouble("rating");  // Assuming "rating" field exists

                        if (driverName != null) {
                            name.setText(driverName);  // Set the name in the TextView
                        }

                        if (ratingValue != null) {
                            // Set the rating as text in the TextView
                            String formattedRating = String.format("%.2f", ratingValue);


                            rating.setText(formattedRating);
                             // Convert the rating to string
                        }
                    } else {
                        // Handle case where the document doesn't exist
                        name.setText("No name available");
                        rating.setText("0");  // Set default value for rating
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to retrieve data
                    name.setText("Error loading data");
                    rating.setText("0");
                });
        currentWorkingHours = view.findViewById(R.id.currentworkinghours);
        saveButton = view.findViewById(R.id.saveButton);
        fetchCurrentWorkingHours();
        saveButton.setOnClickListener(v -> {
            String workingHours = workingHoursInput.getText().toString().trim();

            if (workingHours.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter working hours", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchCurrentWorkingHours();
            // Update Firestore with the new field
            updateWorkingHours(workingHours);
        });
        rideList = new ArrayList<>();
        adapter = new DriverHistoryAdapter(rideList);

        driverHistoryRecyclerView = view.findViewById(R.id.driverHistoryRecyclerView);
        driverHistoryRecyclerView.setAdapter(adapter);
        driverHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fetchRideHistory(); // Fetch rides from Firestore
        return view;

    }
    private void fetchRideHistory() {
        if (user != null) {
            String uid = user.getUid();

            db.collection("rides")
                    .whereEqualTo("driverId", uid)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        rideList.clear(); // Clear old data
                        rideList.addAll(queryDocumentSnapshots.toObjects(Ride.class)); // Add new data
                        adapter.notifyDataSetChanged(); // Notify adapter
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to fetch ride history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void fetchCurrentWorkingHours() {
        if (user != null) {
            String uid = user.getUid();

            db.collection("user")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String newworkingHours = documentSnapshot.getString("workingHours");
                            if (newworkingHours != null) {
                                currentWorkingHours.setText(newworkingHours); // Set current working hours
                            } else {
                                currentWorkingHours.setText("00:00-00:00"); // Default if not found
                            }
                        } else {
                            currentWorkingHours.setText("00:00-00:00");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to fetch working hours", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void updateWorkingHours(String workingHours) {
        if (user != null) {
            String uid = user.getUid();

            // Update Firestore document
            Map<String, Object> updates = new HashMap<>();
            updates.put("workingHours", workingHours);

            db.collection("user")
                    .document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getActivity(), "Working hours saved successfully!", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to save working hours: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getActivity(), "User not authenticated!", Toast.LENGTH_SHORT).show();
        }
    }
}