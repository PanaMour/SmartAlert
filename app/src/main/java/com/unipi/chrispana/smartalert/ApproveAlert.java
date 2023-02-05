package com.unipi.chrispana.smartalert;

import static java.lang.Double.parseDouble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ApproveAlert extends AppCompatActivity {

    TextView event, location, timestamp, count, comments;
    EditText instructions;
    ImageView image;
    Button accept, reject;
    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference rejectReference;
    DatabaseReference acceptReference;
    StorageReference storageReference;
    CheckBox checkBoxSimilar, checkBoxInstructions;
    public static final double earthRadius = 6371.0;
    int hours = 0;
    int kilometers = 0;
    String message = "";
    AlertClass alertClass;
    String targetToken = "";
    Resources resources;
    String eventENG;
    FirebaseAuth mAuth;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_alert);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.approveAlert));
        mAuth = FirebaseAuth.getInstance();
        resources = getResources();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("alerts");
        event = findViewById(R.id.eventContent);
        location = findViewById(R.id.locationContent);
        location.setMovementMethod(new ScrollingMovementMethod());
        location.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        comments = findViewById(R.id.commentsContent);
        comments.setMovementMethod(new ScrollingMovementMethod());
        comments.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        timestamp = findViewById(R.id.timestampContent);
        count = findViewById(R.id.countContent);

        instructions = findViewById(R.id.addInstuctions);
        image = findViewById(R.id.imageApprove);
        accept = findViewById(R.id.acceptButton);
        reject = findViewById(R.id.rejectButton);
        checkBoxSimilar = findViewById(R.id.notificationAll);
        checkBoxInstructions = findViewById(R.id.customInstructions);
        checkBoxInstructions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                instructions.setEnabled(isChecked);
            }
        });
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for(DataSnapshot alertSnapshot : task.getResult().getChildren()){
                        if(alertSnapshot.child("id").getValue().equals(getIntent().getStringExtra("id"))) {
                            alertClass = alertSnapshot.getValue(AlertClass.class);
                            int stringResourceId = resources.getIdentifier(alertSnapshot.child("event").getValue(String.class), "string", "com.unipi.chrispana.smartalert");
                            String associatedString = resources.getString(stringResourceId);
                            eventENG = alertSnapshot.child("event").getValue(String.class);
                            event.setText(associatedString);
                            String loc = alertSnapshot.child("location").getValue(String.class);
                            try {
                                String city = geocoder.getFromLocation(parseDouble(loc.substring(0,loc.indexOf(","))),parseDouble(loc.substring(loc.indexOf(",")+1,loc.length())),1).get(0).getAddressLine(0);
                                location.setText(city);
                                System.out.println(city);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            retrieveImage(alertSnapshot.child("photo").getValue(String.class),image,alertSnapshot.child("event").getValue(String.class));
                            timestamp.setText(alertSnapshot.child("timestamp").getValue(String.class));
                            count.setText(String.valueOf(alertSnapshot.child("count").getValue(Integer.class)));
                            comments.setText(alertSnapshot.child("comments").getValue(String.class));
                        }
                    }
                }
                else {
                    Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
                }
            }
        });
    }
    public void accept(View view){
        /*database = FirebaseDatabase.getInstance();
        reference = database.getReference("alerts");
        acceptReference = database.getReference("accepted");
        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for(DataSnapshot alertSnapshot : task.getResult().getChildren()){
                        switch (alertClass.getEvent()){
                            case "Earthquake":
                                hours = 2;
                                kilometers = 150;
                                break;
                            case "Flood":
                                hours = 12;
                                kilometers = 100;
                                break;
                            case "Hurricane":
                                hours = 24;
                                kilometers = 80;
                                break;
                            case "Fire":
                                hours = 48;
                                kilometers = 200;
                                break;
                            case "Storm":
                                hours = 5;
                                kilometers = 50;
                                break;
                        }
                        if(isWithinHours(alertSnapshot.child("timestamp").getValue(String.class),alertClass.getTimestamp(),hours) &&
                                alertSnapshot.child("event").getValue(String.class).equals(alertClass.getEvent()) &&
                                isWithinKilometers(alertSnapshot.child("location").getValue(String.class),alertClass.getLocation(),kilometers)&&
                                !alertSnapshot.child("id").getValue(String.class).equals(alertClass.getId())){
                            if(!checkBoxSimilar.isChecked())
                                reference.child(alertSnapshot.getKey()).child("count").setValue(alertSnapshot.child("count").getValue(Integer.class)-1);
                            else{
                                System.out.println(alertSnapshot);
                                acceptReference.child(alertSnapshot.getKey()).setValue(alertSnapshot.getValue(AlertClass.class));
                                reference.child(alertSnapshot.getKey()).removeValue();
                            }
                        }
                    }
                    acceptReference.child(alertClass.getId()).setValue(alertClass);
                    reference.child(alertClass.getId()).removeValue();
                    onBackPressed();
                }
                else {
                    Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
                }
            }
        });*/

        sendNotification();
    }

    private void sendNotification(){
        reference = database.getReference("all_users");
        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for(DataSnapshot alertSnapshot : task.getResult().getChildren()){
                        if (alertSnapshot.child("role").getValue(String.class).equals("user")) {
                            switch (eventENG) {
                                case "Earthquake":
                                    kilometers = 150;
                                    message = getString(R.string.alertEarthquake);
                                    break;
                                case "Flood":
                                    kilometers = 100;
                                    message = getString(R.string.alertFlood);
                                    break;
                                case "Hurricane":
                                    kilometers = 80;
                                    message = getString(R.string.alertHurricane);
                                    break;
                                case "Fire":
                                    kilometers = 200;
                                    message = getString(R.string.alertFire);
                                    break;
                                case "Storm":
                                    kilometers = 50;
                                    message = getString(R.string.alertStorm);
                                    break;
                            }
                            targetToken = alertSnapshot.child("token").getValue(String.class);
                            reference.child(alertSnapshot.child("uid").getValue(String.class)).child("eventLocation").setValue(alertClass.getLocation());
                            reference.child(alertSnapshot.child("uid").getValue(String.class)).child("title").setValue(eventENG);
                            if(checkBoxInstructions.isChecked())
                                reference.child(alertSnapshot.child("uid").getValue(String.class)).child("message").setValue(instructions.getText().toString());
                            else
                                reference.child(alertSnapshot.child("uid").getValue(String.class)).child("message").setValue(message);
                            reference.child(alertSnapshot.child("uid").getValue(String.class)).child("startTracking").setValue(true);
                        }
                    }
                    reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                for(DataSnapshot alertSnapshot : task.getResult().getChildren()){
                                    if(alertSnapshot.child("role").getValue(String.class).equals("user"))
                                        reference.child(alertSnapshot.child("uid").getValue(String.class)).child("startTracking").setValue(false);
                                }
                            }else {
                                Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
                            }
                        }
                    });
                }
                else {
                    Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
                }
            }
        });
    }

    public void reject(View view){
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("alerts");
        rejectReference = database.getReference("rejected");
        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for(DataSnapshot alertSnapshot : task.getResult().getChildren()){
                        switch (alertClass.getEvent()){
                            case "Earthquake":
                                hours = 2;
                                kilometers = 150;
                                break;
                            case "Flood":
                                hours = 12;
                                kilometers = 100;
                                break;
                            case "Hurricane":
                                hours = 24;
                                kilometers = 80;
                                break;
                            case "Fire":
                                hours = 48;
                                kilometers = 200;
                                break;
                            case "Storm":
                                hours = 5;
                                kilometers = 50;
                                break;
                        }
                        if(isWithinHours(alertSnapshot.child("timestamp").getValue(String.class),alertClass.getTimestamp(),hours) &&
                                alertSnapshot.child("event").getValue(String.class).equals(alertClass.getEvent()) &&
                                isWithinKilometers(alertSnapshot.child("location").getValue(String.class),alertClass.getLocation(),kilometers)&&
                        !alertSnapshot.child("id").getValue(String.class).equals(alertClass.getId())){
                            //uncomment if you want rejected tables' records to have count equal to 1
                            //alertClass.setCount(alertClass.getCount()-1);
                            reference.child(alertSnapshot.getKey()).child("count").setValue(alertSnapshot.child("count").getValue(Integer.class)-1);
                        }
                    }
                    System.out.println(alertClass);
                    rejectReference.child(alertClass.getId()).setValue(alertClass);
                    reference.child(alertClass.getId()).removeValue();
                    onBackPressed();
                }
                else {
                    Log.d("Task was not successful", String.valueOf(task.getResult().getValue()));
                }
            }
        });
    }

    public boolean isWithinHours(String timestamp1, String timestamp2, int n) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            long diff = dateFormat.parse(timestamp1).getTime() - dateFormat.parse(timestamp2).getTime();
            return Math.abs(diff) <= (long) n * 60 * 60 * 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    //calculate the distance between 2 points using Haversine Formula
    public static boolean isWithinKilometers(String location1, String location2, double n) {
        String[] latLong1 = location1.split(",");
        String[] latLong2 = location2.split(",");
        double lat1 = Double.parseDouble(latLong1[0]);
        double lon1 = Double.parseDouble(latLong1[1]);
        double lat2 = Double.parseDouble(latLong2[0]);
        double lon2 = Double.parseDouble(latLong2[1]);

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        return distance <= n;
    }

    public void retrieveImage(String imageID,ImageView photo,String event){
        if(!imageID.equals(""))
            storageReference = FirebaseStorage.getInstance().getReference(imageID);
        else
            storageReference = FirebaseStorage.getInstance().getReference(event);

        try {
            File localfile = File.createTempFile("tempfile", "");

            storageReference.getFile(localfile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                            photo.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }
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
}