package com.unipi.chrispana.smartalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DatabaseListenerService extends Service {

    final String API_KEY = "AAAAk9DnW7g:APA91bFi1U3wqq06Qr8Tcu7q_aNwZ6OljByXy6kGIB9Zw-zGbCz9Q_sChqjime6kMArS8zrpm0zv6cEsTwMkF6La9ZWtVi7XN0dVSHu0IGtgV4Qy-gkCzWlrXHDHj9860SNPnxJh4w7W";
    private static final int NOTIFICATION_ID = 123;
    FirebaseAuth mAuth;
    FirebaseUser user;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(1, createNotification());
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("all_users").child(user.getUid());
        // Listen to the database and perform actions when data changes
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Perform actions in response to data changes
                if(dataSnapshot.child("startTracking").getValue(Boolean.class)){
                    Intent serviceIntent = new Intent(DatabaseListenerService.this, LocationService.class);
                    serviceIntent.putExtra("userid",user.getUid());
                    serviceIntent.putExtra("title",dataSnapshot.child("title").getValue(String.class));
                    serviceIntent.putExtra("eventLocation",dataSnapshot.child("eventLocation").getValue(String.class));
                    serviceIntent.putExtra("token",dataSnapshot.child("token").getValue(String.class));
                    startService(serviceIntent);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, UserAlert.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "location_service_channel")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Listening for Alerts")
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setContentText("Running...");
        return builder.build();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("location_service_channel",
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
