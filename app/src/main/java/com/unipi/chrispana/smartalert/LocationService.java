package com.unipi.chrispana.smartalert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    DatabaseReference reference;
    String uid;
    String targetToken;
    String title;
    String message;
    String eventLocation;
    final String API_KEY = "AAAAk9DnW7g:APA91bFi1U3wqq06Qr8Tcu7q_aNwZ6OljByXy6kGIB9Zw-zGbCz9Q_sChqjime6kMArS8zrpm0zv6cEsTwMkF6La9ZWtVi7XN0dVSHu0IGtgV4Qy-gkCzWlrXHDHj9860SNPnxJh4w7W";
    String url = "https://fcm.googleapis.com/fcm/send";
    public static final double earthRadius = 6371.0;
    int kilometers = 0;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(Location location) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
                System.out.println(location);
                String loc = location.getLatitude()+ "," + location.getLongitude();
                reference = FirebaseDatabase.getInstance().getReference("all_users").child(uid);

                switch (title){
                    case "Earthquake":
                        kilometers = 150;
                        break;
                    case "Flood":
                        kilometers = 100;
                        break;
                    case "Hurricane":
                        kilometers = 80;
                        break;
                    case "Fire":
                        kilometers = 200;
                        break;
                    case "Storm":
                        kilometers = 50;
                        break;
                }
                reference.child("location").setValue(loc);
                reference.child("startTracking").setValue(false);
                if(isWithinKilometers(loc,eventLocation,kilometers)){
                    StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Handle response from the server
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle error
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Authorization", "key=" + API_KEY);
                            headers.put("Content-Type", "application/json");
                            return headers;
                        }

                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("to", targetToken);

                                JSONObject data = new JSONObject();
                                data.put("title", "Watch out! "+title+"!");
                                data.put("content", message);

                                jsonObject.put("data", data);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            return jsonObject.toString().getBytes();
                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(LocationService.this);
                    requestQueue.add(request);
                }
                locationManager.removeUpdates(locationListener);
                stopSelf();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        uid = intent.getStringExtra("userid");
        title = intent.getStringExtra("title");
        eventLocation = intent.getStringExtra("eventLocation");
        targetToken = intent.getStringExtra("token");
        message = intent.getStringExtra("message");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

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
}