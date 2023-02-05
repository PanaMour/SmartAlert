package com.unipi.chrispana.smartalert;

import static androidx.browser.customtabs.CustomTabsClient.getPackageName;
import static java.lang.Double.parseDouble;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.MyViewHolder> {

    StorageReference storageReference;
    Context context;
    ArrayList<AlertClass> list;
    Resources resources;

    public AlertAdapter(Context context, ArrayList<AlertClass> list, Resources resources) {
        this.context = context;
        this.list = list;
        this.resources = resources;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item,parent,false);

        return new MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        AlertClass alertClass = list.get(position);
        int stringResourceId = resources.getIdentifier(alertClass.getEvent(), "string", "com.unipi.chrispana.smartalert");
        String associatedString = resources.getString(stringResourceId);

        holder.event.setText(associatedString);
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String loc = alertClass.getLocation();
        String city;
        try {
            city = geocoder.getFromLocation(parseDouble(loc.substring(0,loc.indexOf(","))),parseDouble(loc.substring(loc.indexOf(",")+1,loc.length())),1).get(0).getLocality();
        } catch (IOException e) {
            city = loc;
        }
        holder.location.setText(city);
        holder.time.setText(alertClass.getTimestamp());
        retrieveImage(alertClass.getPhoto(),holder.photo,alertClass.getEvent());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ApproveAlert.class);
                intent.putExtra("id",alertClass.getId());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView event, location, time;
        ImageView photo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            photo = itemView.findViewById(R.id.eventImage1);
            event = itemView.findViewById(R.id.eventTextView);
            location = itemView.findViewById(R.id.locationTextView);
            time = itemView.findViewById(R.id.timeTextView);

        }
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

}
