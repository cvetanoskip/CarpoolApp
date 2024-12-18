package com.example.carpool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DriverHistoryAdapter extends RecyclerView.Adapter<DriverHistoryAdapter.DriverRideViewHolder> {
    private List<Ride> rides;

    public DriverHistoryAdapter(List<Ride> rides) {
        this.rides = rides;
    }

    @NonNull
    @Override
    public DriverRideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride_driver, parent, false);
        return new DriverRideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverRideViewHolder holder, int position) {
        Ride ride = rides.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user")
                .document(ride.getPassengerId()) // Use passengerId to fetch the user's document
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String passengerName = documentSnapshot.getString("Name"); // Assuming "name" field exists
                        holder.passengerNameTextView.setText(passengerName);
                    } else {
                        holder.passengerNameTextView.setText("Unknown Passenger"); // Fallback text
                    }
                })
                .addOnFailureListener(e -> {
                    holder.passengerNameTextView.setText("Error Loading Name"); // Error handling
                });
        holder.costTextView.setText(String.format("%.2f", ride.getCost()) + " MKD");
        holder.distanceTextView.setText(String.format("%.2f", ride.getDistance()) + " km");

        // Set initial rating
        holder.passengerRatingBar.setRating(ride.getPassengerRating());

        // Listener for rating changes
        holder.passengerRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            private boolean isSaved = false;

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser && !isSaved) {
                    isSaved = true;

                    // Save the rating to the user's collection
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("user")
                            .document(ride.getPassengerId()) // Use passengerId to locate user
                            .update("rating", rating) // Update or set the rating field
                            .addOnSuccessListener(aVoid -> {
                                // Rating saved successfully
                                Toast.makeText(holder.itemView.getContext(), "Rating saved to user!", Toast.LENGTH_SHORT).show();
                                isSaved = false;
                            })
                            .addOnFailureListener(e -> {
                                // Handle error
                                Toast.makeText(holder.itemView.getContext(), "Failed to save rating!", Toast.LENGTH_SHORT).show();
                                isSaved = false;
                            });
                }
            }
        });
    }
    private void savePassengerRating(String passengerId, float rating) {
        // Example: Save the rating to Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rides")
                .document(passengerId) // Use passenger ID or a unique ride ID
                .update("passengerRating", rating)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }
    @Override
    public int getItemCount() {
        return rides.size();
    }

    public class DriverRideViewHolder extends RecyclerView.ViewHolder {
        TextView passengerNameTextView, costTextView, distanceTextView;
        RatingBar passengerRatingBar;

        public DriverRideViewHolder(@NonNull View itemView) {
            super(itemView);
            passengerNameTextView = itemView.findViewById(R.id.passengerNameTextView);
            costTextView = itemView.findViewById(R.id.costTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            passengerRatingBar = itemView.findViewById(R.id.passengerRatingBar);
        }
    }
}
