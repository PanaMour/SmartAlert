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

    FirebaseAuth mAuth;
    FirebaseUser user;
    //If this service has been started with action "CLOSE" it stops itself, otherwise it continuously listens for changes in the Database.
    //If the startTracking value changes then a new Location Service is being started with all the data needed passed to it.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(1, createNotification());
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (intent != null && "CLOSE".equals(intent.getAction())) {
            stopSelf();
            return Service.START_STICKY;
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("all_users").child(user.getUid());
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
                    serviceIntent.putExtra("message",dataSnapshot.child("message").getValue(String.class));
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

    //Creates a notification that informs the user that the application is running in the background.
    private Notification createNotification() {
        Intent intent = new Intent(this, UserAlert.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent,PendingIntent.FLAG_MUTABLE );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "listener_service_channel")
                .setSmallIcon(R.drawable.logofg)
                .setContentTitle(getString(R.string.listening))
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.logofg, getString(R.string.stoplistening), cancelPendingIntent);
        return builder.build();
    }
    //Creates a notification Channel that is mandatory so that the above notification is displayed and the service will be running in the background.
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("listener_service_channel",
                    "Listener Service Channel",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
