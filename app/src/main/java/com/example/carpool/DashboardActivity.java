package com.example.carpool;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logoutbutton);
        //textView=findViewById(R.id.textView2);
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {

            FragmentManager fragmentManager = getSupportFragmentManager();
            String uid = user.getUid();
           // textView.setText(uid);
            db.collection("user")
                    .document(uid)// Replace with your actual document ID
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            //textView.setText(role);
                            if ( role.equals("Driver")) {  // Check if role is "Driver"
                                DriverDashboard driverFragment = new DriverDashboard();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainerView, driverFragment, null)
                                        .setReorderingAllowed(true)
                                        .addToBackStack("name") // Name can be null
                                        .commit();
                            } else if (  role.equals("Passenger")) {
                                PassengerDashboard passengerFragment = new PassengerDashboard();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainerView, passengerFragment, null)
                                        .setReorderingAllowed(true)
                                        .addToBackStack("name") // Name can be null
                                        .commit();

                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                    });

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}