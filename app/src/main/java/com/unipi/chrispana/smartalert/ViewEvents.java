package com.unipi.chrispana.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.chrispana.smartalert.databinding.ActivityViewEventsBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ViewEvents extends AppCompatActivity {
    ActivityViewEventsBinding binding;
    RecyclerView recyclerView;
    DatabaseReference database;
    AlertAdapter alertAdapter;
    ArrayList<AlertClass> list;
    private static final int TIME_INTERVAL = 2000; // 2 seconds
    private long mBackPressed;
    Resources resources;
    FirebaseAuth mAuth;
    //Gets all the events that are ongoing from the database.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.viewEvents));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.logofg);
        recyclerView = findViewById(R.id.eventList);
        database = FirebaseDatabase.getInstance().getReference("alerts");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        list = new ArrayList<>();
        resources = getResources();
        alertAdapter = new AlertAdapter(this,list,resources);
        recyclerView.setAdapter(alertAdapter);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    AlertClass alertClass = dataSnapshot.getValue(AlertClass.class);
                    list.add(alertClass);
                }
                Collections.sort(list, Comparator.comparing(AlertClass::getCount).reversed());
                alertAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //Adds logout to action bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }
    //Signs out and redirects to MainActivity.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(ViewEvents.this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(getBaseContext(), getString(R.string.toastSucLogout), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}