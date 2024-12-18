package com.example.carpool;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PassengerDashboard extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LatLng destinationLatLng;
    private LatLng currentLatLng;
    private RecyclerView recyclerView;
    private DriverAdapter driverAdapter;
    private List<Driver> driverList = new ArrayList<>();
    private Button selectDriverButton;
    private RecyclerView ridesRecyclerView;
    private RideHistoryAdapter ridesAdapter;
    private List<Ride> ridesList;
    private Button loadRidesButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_dashboard, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewDrivers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the "Select Driver" button
        selectDriverButton = view.findViewById(R.id.selectDriverButton);

        // Fetch the drivers from Firestore when the button is clicked
        selectDriverButton.setOnClickListener(v -> fetchDriversFromFirestore());
        // Initialize the Map Fragment and get the map asynchronously
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        ridesRecyclerView = view.findViewById(R.id.rvRideHistory);
        loadRidesButton = view.findViewById(R.id.btnLoadRides);
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        ridesList = new ArrayList<>();
        ridesAdapter = new RideHistoryAdapter(ridesList);
        ridesRecyclerView.setAdapter(ridesAdapter);

        loadRidesButton.setOnClickListener(v -> loadPassengerRideHistory());
        return view;
    }
    private void fetchDriversFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query to get drivers with role "Driver"
        db.collection("user")
                .whereEqualTo("role", "Driver")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        driverList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Driver driver = document.toObject(Driver.class);
                                driver.setId(document.getId());
                                String pricePerKm = document.getString("Cost");
                                // Use the appropriate method for retrieving the field
                                driver.setPricePerKm(pricePerKm);// Set the Firestore document ID as the driver's ID
                                driverList.add(driver);
                            } catch (Exception e) {
                                // Catch potential issues while creating the driver object
                                Log.e("Firestore", "Error creating Driver object: " + e.getMessage());
                            }
                        }
                        // Update the RecyclerView with the driver list
                        if (driverAdapter == null) {
                            driverAdapter = new DriverAdapter(driverList, getContext(),0);
                            recyclerView.setAdapter(driverAdapter);
                        } else {
                            driverAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("Firestore", "Error getting drivers: " + task.getException().getMessage());
                        Toast.makeText(getContext(), "Error getting drivers: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Check if permission is granted and enable the location layer on the map
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Move the camera to the current location
        getUserLocation();

        // Set an onClickListener for selecting a destination
        mMap.setOnMapClickListener(latLng -> {
            // Add a marker at the clicked location
            if (destinationLatLng != null) {
                mMap.clear(); // Remove previous marker
            }
            destinationLatLng = latLng;
            mMap.addMarker(new MarkerOptions().position(latLng).title("Destination"));

            // Now calculate the distance
            calculateDistance();
        });
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        });
    }
    private void loadPassengerRideHistory() {
        // Clear the current list to avoid duplicating items
        ridesList.clear();
        ridesAdapter.notifyDataSetChanged();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String passengerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("rides")
                .whereEqualTo("passengerId", passengerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Ride ride = document.toObject(Ride.class);
                            if (ride != null) {
                                ridesList.add(ride);
                            } else {
                                Log.e("RideHistory", "Ride object is null for document: " + document.getId());
                            }
                        }
                        ridesAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("RideHistory", "No rides found for this user");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RideHistory", "Error fetching rides", e);
                });

    }


    private void calculateDistance() {
        if (currentLatLng != null && destinationLatLng != null) {
            Location currentLocation = new Location("");
            currentLocation.setLatitude(currentLatLng.latitude);
            currentLocation.setLongitude(currentLatLng.longitude);

            Location destinationLocation = new Location("");
            destinationLocation.setLatitude(destinationLatLng.latitude);
            destinationLocation.setLongitude(destinationLatLng.longitude);

            // Calculate the distance in meters
            float distanceInMeters = currentLocation.distanceTo(destinationLocation);

            // Convert meters to kilometers (optional)
            float distanceInKilometers = distanceInMeters / 1000;

            // Display the distance on the UI (e.g., a TextView)
            TextView distanceTextView = getView().findViewById(R.id.distanceTextView);
            distanceTextView.setText("Distance: " + String.format("%.2f", distanceInKilometers) + " km");
            if (driverAdapter != null) {
                driverAdapter.updateDistance(distanceInKilometers);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location
                if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // Permission denied, handle the case (e.g., show a message to the user)
            }
        }
    }
}
