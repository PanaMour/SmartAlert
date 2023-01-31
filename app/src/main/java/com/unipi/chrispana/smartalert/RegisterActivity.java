package com.unipi.chrispana.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity implements LocationListener{

    EditText email, password, confirmpassword;
    FirebaseAuth mAuth;
    String token = "";
    FirebaseDatabase database;
    DatabaseReference reference;
    LocationManager locationManager;
    String location = "";
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        confirmpassword = findViewById(R.id.confirmPassword);
        mAuth = FirebaseAuth.getInstance();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        getLocation();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Could not access the token");
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();

                        System.out.println("Here is the Token: "+token);
                    }
                });
    }
    public void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Enabled");
            builder.setMessage("Please enable Location Services");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (loc != null) {
                location = loc.getLatitude()+ "," + loc.getLongitude();
            }
        }
    }
    public void register(View view){
        if(!email.getText().toString().equals("") && !password.getText().toString().equals("")) {
            if (password.getText().toString().equals(confirmpassword.getText().toString())) {
                if (location.equals(""))
                    showMessage("Error!","An error occurred. Please try again!");
                else {
                    database = FirebaseDatabase.getInstance();
                    reference = database.getReference("all_users");
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        user = mAuth.getCurrentUser();
                                        reference.child(user.getUid()).child("uid").setValue(user.getUid());
                                        reference.child(user.getUid()).child("token").setValue(token);
                                        reference.child(user.getUid()).child("location").setValue(location);
                                        reference.child(user.getUid()).child("startTracking").setValue(false);
                                        reference.child(user.getUid()).child("role").setValue("user");
                                        showMessage("Success!", "User registered.");
                                    } else {
                                        showMessage("Error", task.getException().getLocalizedMessage());
                                    }
                                }
                            });
                }
            } else {
                showMessage("Error", "Passwords do not match.");
            }
        }
        else{
            showMessage("Error", "Please fill in the form.");
        }
    }

    public void back(View view){
        onBackPressed();
    }

    public void showMessage(String title, String text){
        new android.app.AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(text)
                .show();
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}