package com.example.carpool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder> {
    private List<Ride> rides;

    public RideHistoryAdapter(List<Ride> rides) {
        this.rides = rides;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rides.get(position);
        holder.driverNameTextView.setText(ride.getDriverName() != null ? ride.getDriverName() : "Unknown");
        holder.costTextView.setText(String.format("%.2f MKD", ride.getCost()));
        holder.distanceTextView.setText(String.format("%.2f km", ride.getDistance()));
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    public class RideViewHolder extends RecyclerView.ViewHolder {
        TextView driverNameTextView, costTextView, distanceTextView;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            driverNameTextView = itemView.findViewById(R.id.driverNameTextView);
            costTextView = itemView.findViewById(R.id.costTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
        }
    }
}

