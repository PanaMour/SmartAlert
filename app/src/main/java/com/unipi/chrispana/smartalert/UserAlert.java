package com.unipi.chrispana.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserAlert extends AppCompatActivity {

    Spinner events;
    EditText comments;
    TextView location;
    Button insertAlert;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_alert);

        events = findViewById(R.id.insertEvent);
        String eq = getString(R.string.earthquake);
        String flood = getString(R.string.flood);
        String hurricane = getString(R.string.hurricane);
        String fire = getString(R.string.fire);
        String[] eventSpinner = new String[]{eq, flood, hurricane, fire};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, eventSpinner);
        events.setAdapter(adapter);
        comments = findViewById(R.id.insertComments);
        location = findViewById(R.id.insertLocation);
        insertAlert = findViewById(R.id.insertAlert);
    }

    public void insertAlert(View view){
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("alerts");

        String id = reference.child("id").push().toString();
        String event = events.getSelectedItem().toString();
        String comment = comments.getText().toString();
        String userLocation = location.getText().toString();
        String timestamp = java.time.LocalDate.now().toString();
        String photo = "photo";

        AlertClass alertClass = new AlertClass(id,event,comment,userLocation,timestamp,photo);
        reference.child(id).setValue(alertClass);

        Toast.makeText(this,"Alert has been sent successfully!", Toast.LENGTH_SHORT).show();
        comments.setText("");

    }
}