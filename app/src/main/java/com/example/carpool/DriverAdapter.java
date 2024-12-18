package com.example.carpool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private List<Driver> driverList;
    private Context context;
    private float distanceToDestination;
    public DriverAdapter(List<Driver> driverList, Context context,float distanceToDestination) {
        this.driverList = driverList;
        this.context = context;
        this.distanceToDestination = distanceToDestination;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.driver_item, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);

        // Set the driver information on the item view
        holder.nameTextView.setText(driver.getName());
        holder.ratingTextView.setText("Rating: " + driver.getRating());
        String pricePerKm = driver.getPricePerKm();
        if (pricePerKm == null || pricePerKm.isEmpty()) {
            holder.priceTextView.setText("Price per km: Not available");
        } else {
            holder.priceTextView.setText("Price per km: " + pricePerKm + " MKD");
        }
        double cost = distanceToDestination * Float.parseFloat(pricePerKm);
        holder.costTextView.setText("Cost: " + String.format("%.2f", cost) + " MKD");
        holder.vehicleTextView.setText("Vehicle: " + driver.getVehicle());
        holder.workinghoursTextView.setText("Working hours: " + driver.getWorkingHours());
        // Optionally, handle clicks to select the driver
        holder.itemView.setOnClickListener(v -> {
            // Handle driver selection logic here
           // float distance = distanceToDestination; // Replace with actual distance
            DriverPopupFragment fragment = new DriverPopupFragment(
                    driver.getId(),
                    driver.getName(),
                    cost,  // Send the calculated cost
                    distanceToDestination,
                    driver.getRating()
            );
            fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "DriverPopup");

            Toast.makeText(context, "Selected " + driver.getName(), Toast.LENGTH_SHORT).show();
        });
    }
    public void updateDistance(float newDistance) {
        this.distanceToDestination = newDistance;
        notifyDataSetChanged(); // This will refresh the data in the adapter, if necessary
    }
    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, ratingTextView, priceTextView,costTextView,vehicleTextView,workinghoursTextView;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.driverName);
            ratingTextView = itemView.findViewById(R.id.driverRating);
            priceTextView = itemView.findViewById(R.id.driverPrice);
            costTextView=itemView.findViewById(R.id.costTextView);
            vehicleTextView = itemView.findViewById(R.id.driverAuto);  // New field
            workinghoursTextView = itemView.findViewById(R.id.workingTime);  // New field
        }
    }
}
