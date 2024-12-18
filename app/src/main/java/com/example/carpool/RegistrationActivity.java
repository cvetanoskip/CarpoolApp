package com.example.carpool;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    TextInputEditText editTextEmail,editTextPassword,editName,editAge,editVehicle,editCost;
    RadioGroup role;
    TextView textView;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    FirebaseFirestore db;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent= new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        editName=findViewById(R.id.name);
        editAge=findViewById(R.id.age);
        editVehicle=findViewById(R.id.vozilo);
        editCost=findViewById(R.id.Costperkm);
        editVehicle.setVisibility(View.GONE);
        editCost.setVisibility(View.GONE);
        role=findViewById(R.id.rolegroup);
        textView=findViewById(R.id.loginnow);
        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        editTextEmail=findViewById(R.id.email);
        editTextPassword=findViewById(R.id.password);
        buttonReg=findViewById(R.id.registerbutton);
        progressBar=findViewById(R.id.ProgressBar);
        role.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.driver)
                {
                    editVehicle.setVisibility(View.VISIBLE);
                    editCost.setVisibility(View.VISIBLE);
                }
                else if(i==R.id.passenger)
                {
                    editVehicle.setVisibility(View.GONE);
                    editCost.setVisibility(View.GONE);
                }
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email,password,name,age,vehicle,cost;
                email=String.valueOf(editTextEmail.getText());
                password=String.valueOf(editTextPassword.getText());
                name=String.valueOf(editName.getText());
                age=String.valueOf(editAge.getText());
                cost=String.valueOf(editCost.getText());
                vehicle=String.valueOf(editVehicle.getText());

                progressBar.setVisibility(View.VISIBLE);
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RegistrationActivity.this,"Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(RegistrationActivity.this,"Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    String uid = task.getResult().getUser().getUid();
                                    Map<String,Object> user=new HashMap<>();
                                    user.put("E-mail",email);
                                    user.put("Password",password);
                                    user.put("Name",name);
                                    user.put("Age",age);
                                    user.put("id",uid);

                                    if(role.getCheckedRadioButtonId()==R.id.driver)
                                    {
                                        user.put("Vehicle",vehicle);
                                        user.put("Cost",cost);
                                        user.put("role","Driver");
                                    }
                                    else{
                                        user.put("role","Passenger");
                                    }
                                    db.collection("user").document(uid).set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(RegistrationActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(RegistrationActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                    Toast.makeText(RegistrationActivity.this, "Account Created.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent= new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    // If sign in fails, display a message to the user.

                                    Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });
    }
}