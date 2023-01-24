package com.unipi.chrispana.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static int REQUEST_PERMISSION=1;
    LocationManager locationManager;
    Button login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = findViewById(R.id.loginbutton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        getLocation();
    }

    /*//gets permission from user to access their location
    public void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Build the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Services Not Enabled");
                builder.setMessage("Please enable Location Services");
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
    }*/

    private void requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Permission is needed to access location from your device...")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_PERMISSION);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_PERMISSION);
                        }
                    }).create().show();
        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_PERMISSION);
        }
    }

    private void getLocation()
    {
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))
            {

            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_PERMISSION);
            }
        }
        else {

            requestLocationPermission();

        }
    }

    public void login(View view){
        Intent intent = new Intent(this, UserAlert.class);
        startActivity(intent);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Thanks for enabling the permission", Toast.LENGTH_SHORT).show();

                //do something permission is allowed here....

            } else {
                Toast.makeText(this, "Please allow the Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}