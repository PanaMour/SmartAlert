package com.unipi.chrispana.smartalert;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unipi.chrispana.smartalert.databinding.ActivityMainBinding;
import com.unipi.chrispana.smartalert.databinding.ActivityUserAlertBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class UserAlert extends AppCompatActivity implements LocationListener {

    //retrieve/select image from db
    ActivityUserAlertBinding binding;
    Uri imageUri;
    StorageReference storageReference;
    String fileName = "";
    Spinner events;
    EditText comments;
    LocationManager locationManager;
    Button insertAlert;
    String location = "";
    FirebaseDatabase database;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserAlertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        events = findViewById(R.id.insertEvent);
        String eq = getString(R.string.earthquake);
        String flood = getString(R.string.flood);
        String hurricane = getString(R.string.hurricane);
        String fire = getString(R.string.fire);
        String[] eventSpinner = new String[]{eq, flood, hurricane, fire};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, eventSpinner);
        events.setAdapter(adapter);
        comments = findViewById(R.id.insertComments);
        insertAlert = findViewById(R.id.insertAlert);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        getLocation();

        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result){
                        if(result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            if( data != null && data.getData() != null) {
                                imageUri = data.getData();
                                binding.imageView.setImageURI(imageUri);
                            }
                        }
                    }
                });

        //select image
        binding.selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                someActivityResultLauncher.launch(intent);
            }
        });
    }

    public void uploadImage(){

        AlertDialog.Builder builder = new AlertDialog.Builder(UserAlert.this);
        builder.setMessage("Uploading File...");
        builder.setCancelable(false);
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault());
        Date now = new Date();
        fileName = formatter.format(now);
        storageReference = FirebaseStorage.getInstance().getReference(fileName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        binding.imageView.setImageURI(null);
                        Toast.makeText(UserAlert.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                        if (alertDialog.isShowing())
                            alertDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (alertDialog.isShowing())
                            alertDialog.dismiss();
                        Toast.makeText(UserAlert.this,"Failed to Upload",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
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
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (loc != null) {
                location = loc.getLatitude()+ "," + loc.getLongitude();
            }
        }
    }

    public void insertAlert(View view){
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("alerts");

        if (location.equals(""))
            showMessage("Error!","An error occurred. Please try again!");
        else {

            String event = events.getSelectedItem().toString();
            String comment = comments.getText().toString();
            String timestamp = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
            if(imageUri != null)
                uploadImage();
            String photo = fileName;

            AlertClass alertClass = new AlertClass(event, comment, location, timestamp, photo);

            reference.child("").push().setValue(alertClass);

            Toast.makeText(UserAlert.this, "Alert has been sent successfully!", Toast.LENGTH_SHORT).show();
            comments.setText("");
            imageUri = null;
            fileName = "";
            binding.imageView.setImageDrawable(getResources().getDrawable(R.drawable.insert_photo_here));
        }
    }

    public void showMessage(String title, String text){
        new android.app.AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(text)
                .show();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}