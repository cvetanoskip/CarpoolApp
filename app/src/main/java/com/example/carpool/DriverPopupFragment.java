package com.example.carpool;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DriverPopupFragment extends DialogFragment {

    private String driverId, driverName;
    private float distance; // Pass distance from previous activity/fragment
    private double cost;

    private float driverRating;
    public DriverPopupFragment(String driverId, String driverName, double cost, float distance, float driverRating) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.cost = cost;
        this.distance = distance;
        this.driverRating = driverRating;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_popup, container, false);

        TextView nameTextView = view.findViewById(R.id.driverNameTextView);
        TextView costTextView = view.findViewById(R.id.driverCostTextView);
        TextView distanceTextView = view.findViewById(R.id.driverDistanceTextView);
        RatingBar ratingBar = view.findViewById(R.id.driverRatingBar);



        nameTextView.setText("Driver: " + driverName);
        costTextView.setText("Cost: " + String.format("%.2f", cost) + " MKD");
        distanceTextView.setText("Distance: " + String.format("%.2f", distance) + " km");
        ratingBar.setRating(driverRating);  // Set the initial rating

        // Optionally, allow the passenger to rate the driver
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            // Handle rating changes (optional: update database, save user rating, etc.)
        });

        Button confirmButton = view.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            // Handle driver selection confirmation
            float newRating = ratingBar.getRating();  // Get the new rating from the RatingBar

            // Call a method to handle saving/updating the rating in the database
            updateDriverRatingInDatabase(driverId, newRating);
            saveRideToFirestore(driverId, driverName, cost, distance);
            dismiss();  // Close the dialog after confirming
        });

        return view;
    }
    private void updateDriverRatingInDatabase(String driverId, float newRating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get a reference to the users collection
        db.collection("user")
                .document(driverId)  // Using driverId to access the driver's document
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check if the 'rating' and 'ratingCount' fields exist in the document
                        float existingRating = documentSnapshot.contains("rating") ? documentSnapshot.getDouble("rating").floatValue() : 0;
                        long ratingCount = documentSnapshot.contains("ratingCount") ? documentSnapshot.getLong("ratingCount") : 0;

                        // If rating or ratingCount does not exist, initialize with default values
                        if (ratingCount == 0) {
                            // No previous ratings, so use the new rating as the first entry
                            existingRating = newRating;
                            ratingCount = 1;
                        } else {
                            // Calculate new average rating
                            float totalRating = existingRating * ratingCount + newRating;
                            ratingCount++;
                            existingRating = totalRating / ratingCount;
                        }

                        // Update the document with the new rating and rating count
                        db.collection("user").document(driverId)
                                .update("rating", existingRating, "ratingCount", ratingCount)
                                .addOnSuccessListener(aVoid -> {
                                    // Log success
                                    Log.d("DriverRating", "Rating updated successfully");
                                })
                                .addOnFailureListener(e -> {
                                    // Log error
                                    Log.e("DriverRating", "Error updating rating", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Log error
                    Log.e("DriverRating", "Error fetching driver data", e);
                });
    }
    private void saveRatingToFirestore(String driverId, float newRating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Retrieve the driver document
        db.collection("user").document(driverId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Double currentRating = documentSnapshot.getDouble("rating");
                if (currentRating == null) {
                    // Add the new rating if none exists
                    db.collection("user").document(driverId)
                            .update("rating", newRating)
                            .addOnSuccessListener(aVoid -> {
                                // Handle success (optional)
                            });
                } else {
                    // Calculate the average rating
                    double averageRating = (currentRating + newRating) / 2;
                    db.collection("user").document(driverId)
                            .update("rating", averageRating)
                            .addOnSuccessListener(aVoid -> {
                                // Handle success (optional)
                            });
                }
            }
        });
    }

    private void saveRideToFirestore(String driverId, String driverName, double cost, float distance) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Retrieve the passenger's ID and name
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String passengerId = currentUser != null ? currentUser.getUid() : null;

        // Create the Ride object with the passenger's ID and name
        Ride ride = new Ride(driverId, driverName, cost, distance, passengerId);

        // Save the ride to the Firestore "rides" collection
        db.collection("rides").add(ride)
                .addOnSuccessListener(documentReference -> {
                    // Handle success (optional)
                    Log.d("Ride", "Ride added successfully");
                })
                .addOnFailureListener(e -> {
                    // Handle failure (optional)
                    Log.e("Ride", "Error adding ride", e);
                });
    }


}
