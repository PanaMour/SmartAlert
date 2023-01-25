package com.unipi.chrispana.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.chrispana.smartalert.databinding.ActivityUserAlertBinding;
import com.unipi.chrispana.smartalert.databinding.ActivityViewEventsBinding;

import java.io.File;
import java.io.IOException;

public class ViewEvents extends AppCompatActivity {

    ImageView imageView1;
    TextView textView1;
    ActivityViewEventsBinding binding;
    Uri imageUri;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imageView1 = findViewById(R.id.eventImage1);
        textView1 = findViewById(R.id.details1);
        retrieveImage();
    }

    public void retrieveImage(){
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewEvents.this);
            builder.setMessage("Fetching Image...");
            builder.setCancelable(false);
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();

            String imageID = "earthquake1";

            storageReference = FirebaseStorage.getInstance().getReference(imageID + ".jpg");

            try {
                File localfile = File.createTempFile("tempfile", ".jpg");

                storageReference.getFile(localfile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                if (alertDialog.isShowing())
                                    alertDialog.dismiss();

                                Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                binding.eventImage1.setImageBitmap(bitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (alertDialog.isShowing())
                                    alertDialog.dismiss();
                                Toast.makeText(ViewEvents.this, "Failed to retrieve", Toast.LENGTH_SHORT);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}