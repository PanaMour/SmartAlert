package com.unipi.chrispana.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StartupActivity extends AppCompatActivity implements LocationListener{

    LocationManager locationManager;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("all_users");
            user = mAuth.getCurrentUser();
            reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DataSnapshot alertSnapshot : task.getResult().getChildren()) {
                            if(alertSnapshot.child("uid").getValue().equals(user.getUid())) {
                                if(alertSnapshot.child("role").getValue(String.class).equals("user")){
                                    Intent intent = new Intent(StartupActivity.this, ViewStatistics.class);
                                    startActivity(intent);
                                    return;
                                }
                                else if(alertSnapshot.child("role").getValue(String.class).equals("employee")){
                                    Intent intent = new Intent(StartupActivity.this, ViewEvents.class);
                                    startActivity(intent);
                                    return;
                                }
                            }
                        }
                    }else {
                        Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
                    }
                }
            });
        }else
            setContentView(R.layout.activity_startup);
    }

    public void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Build the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Services Not Enabled!");
                builder.setMessage("Please enable Location Services.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Show location settings when the user acknowledges the alert dialog
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
                Dialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            }
        }
    }
    public void startup(View view) {

        LocationManager locationManager1 = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager1 != null){
            boolean isGPSEnabled = locationManager1.isLocationEnabled();
            if(!isGPSEnabled){
                Toast.makeText(this, getString(R.string.toastLocOn), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                return;
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                boolean areNotificationsEnabled = notificationManager.areNotificationsEnabled();
                if (!areNotificationsEnabled) {
                    Toast.makeText(this, getString(R.string.toastNotifOn), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    startActivity(intent);
                } else {
                    getLocation();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.toastAllowATT), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public void showMessage(String title, String text){
        new android.app.AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(text)
                .show();
    }
}