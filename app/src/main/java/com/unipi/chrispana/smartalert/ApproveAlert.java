package com.unipi.chrispana.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ApproveAlert extends AppCompatActivity {

    TextView event, location, timestamp, count, comments;
    EditText instructions;
    ImageView image;
    Button accept, reject;
    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_alert);
        database = FirebaseDatabase.getInstance();
        //reference = database.getReference("alerts").child(getIntent().getStringExtra("id"));
        reference = database.getReference("alerts");
        event = findViewById(R.id.eventContent);
        location = findViewById(R.id.locationContent);
        timestamp = findViewById(R.id.timestampContent);
        count = findViewById(R.id.countContent);
        comments = findViewById(R.id.commentsContent);
        instructions = findViewById(R.id.addInstuctions);
        image = findViewById(R.id.imageApprove);
        accept = findViewById(R.id.acceptButton);
        reject = findViewById(R.id.rejectButton);

        String id = getIntent().getStringExtra("id");

        reference.child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    for(DataSnapshot alertSnapshot : task.getResult().getChildren()){
                        System.out.println("DSSDdsfdfsfsd"+alertSnapshot.getValue());
                        System.out.println("IDDDDDDDDDDDDD"+getIntent().getStringExtra("id"));
                        if(alertSnapshot.getValue().equals(getIntent().getStringExtra("id"))){
                            System.out.println("IDDDDDDD"+alertSnapshot.getValue());
                            System.out.println("EVNNTTTT"+alertSnapshot.child("event").getValue(String.class));
                            event.setText(alertSnapshot.child(String.valueOf(alertSnapshot.getValue())).child("event").getValue(String.class));
                            location.setText(alertSnapshot.child("location").getValue(String.class));
                            timestamp.setText(alertSnapshot.child("timestamp").getValue(String.class));
                            count.setText(alertSnapshot.child("count").getValue(String.class));
                        }
                    }
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                }
            }
        });








    }
}