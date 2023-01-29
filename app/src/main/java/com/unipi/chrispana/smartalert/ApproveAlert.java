package com.unipi.chrispana.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ApproveAlert extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_alert);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("alerts").child(getIntent().getStringExtra("id"));

    }
}