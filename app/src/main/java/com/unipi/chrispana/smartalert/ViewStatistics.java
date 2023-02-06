package com.unipi.chrispana.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewStatistics extends AppCompatActivity {
    private static final int TIME_INTERVAL = 2000; // 2 seconds
    private long mBackPressed;
    FirebaseAuth mAuth;
    LocationManager locationManager;
    LocationListener locationListener;
    TextView earthInc, floodInc, fireInc, stormInc, hurricaneInc, emailText;
    DatabaseReference reference;
    //Whenever an alert gets approved the database gets updated and depending on the event (Earthquake, Flood, etc) in the path "sent_alerts" values change.
    //In the onCreate() there is an OnDataChange that updates the corresponding fields (textViews).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_statistics);
        mAuth = FirebaseAuth.getInstance();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.viewStatistics));
        earthInc = findViewById(R.id.earthInc);
        floodInc = findViewById(R.id.floodInc);
        fireInc = findViewById(R.id.fireInc);
        stormInc = findViewById(R.id.stormInc);
        hurricaneInc = findViewById(R.id.hurricaneInc);
        emailText = findViewById(R.id.useremailText);
        emailText.setText(mAuth.getCurrentUser().getEmail());
        reference = FirebaseDatabase.getInstance().getReference("sent_alerts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    System.out.println(dataSnapshot.getKey());
                    switch (dataSnapshot.getKey()){
                        case "Earthquake":
                            earthInc.setText(dataSnapshot.getValue(Integer.class).toString());
                            break;
                        case "Flood":
                            floodInc.setText(dataSnapshot.getValue(Integer.class).toString());
                            break;
                        case "Fire":
                            fireInc.setText(dataSnapshot.getValue(Integer.class).toString());
                            break;
                        case "Storm":
                            stormInc.setText(dataSnapshot.getValue(Integer.class).toString());
                            break;
                        case "Hurricane":
                            hurricaneInc.setText(dataSnapshot.getValue(Integer.class).toString());
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //If the user presses the back button twice in 2 seconds the app moves to the background.
    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            moveTaskToBack(true);
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.toastBackAgain), Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }
    //Adds logout to action bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }
    //Closes running service, signs out and redirects to MainActivity.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            Intent closeService = new Intent(this, DatabaseListenerService.class);
            closeService.setAction("CLOSE");
            startService(closeService);
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(getBaseContext(), getString(R.string.toastSucLogout), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //Stops listening for Location updates.
    @Override
    protected void onPause() {
        super.onPause();
        if (locationListener != null)
            locationManager.removeUpdates(locationListener);
        System.out.println("App was paused");
    }
    //Starts listening for Location updates.
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(locationManager != null){
            boolean isGPSEnabled = locationManager.isLocationEnabled();
            if(isGPSEnabled && locationListener!=null){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            }
        }
        Intent serviceIntent = new Intent(ViewStatistics.this, DatabaseListenerService.class);
        startService(serviceIntent);
        System.out.println("App Resumed");
    }
    //Redirects to UserAlert.
    public void insertAlert(View view){
        Intent intent = new Intent(this, UserAlert.class);
        startActivity(intent);
    }
}