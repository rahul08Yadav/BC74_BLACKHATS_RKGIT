package com.example.location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import static android.Manifest.permission.CALL_PHONE;
import static com.example.location.MainActivity.groupId;
import static com.example.location.MainActivity.userID;
import static com.example.location.MainActivity.userName;

public class PoliceLocation extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private Double Latitude, Longitude;
    private FirebaseFirestore db;
    private LatLng mDestination;
    private String mId;
    private LatLng mOrigin;
    private LinearLayout linearLayout;
    private TextView policeName, policeNumber;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button PoliceExit;
    private TextView CallPolice,SendDetails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.police_location);
        db = FirebaseFirestore.getInstance();
        linearLayout = findViewById(R.id.police_detail);
        policeName = findViewById(R.id.police_name);
        policeNumber = findViewById(R.id.policeNumber);
        PoliceExit = findViewById(R.id.PoliceExit);
        CallPolice = findViewById(R.id.callPolice);
        SendDetails = findViewById(R.id.SendDetails);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(PoliceLocation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);

            ActivityCompat.requestPermissions(PoliceLocation.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);

            return;
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.police_map);
        mapFragment.getMapAsync(PoliceLocation.this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        gettingFusedLocations();
        Toast.makeText(PoliceLocation.this,"Click On Marker to Get Details",Toast.LENGTH_LONG).show();

        db.collection("patrolling")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("POLICE_LOCATION", document.getId() + " => " + document.getData());
                               String latitudePolice = document.getString("latitude");
                                String longitudePolice = document.getString("longitude");

                                LatLng position = new LatLng(Double.parseDouble(latitudePolice),Double.parseDouble(longitudePolice));
                                map.addMarker(new MarkerOptions().position(position).title(document.getId()).icon(BitmapDescriptorFactory.fromResource(R.drawable.police_car   )));//BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            }
                        } else {
                            Log.d("POLICE_LOCATION", "Error getting documents: ", task.getException());
                        }
                    }
                });



        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mDestination = marker.getPosition();
                mId = marker.getTitle();

                if(mId != null && mDestination != null)
                    showDetails(mId);
                return false;
            }
        });
    }


    public void gettingFusedLocations(){
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {


                        // Got last known location. In some rare situations this can be null.

                        if (location != null) {

                            Longitude = location.getLongitude();
                            Latitude = location.getLatitude();
                            // Logic to handle location object
                            Log.d("LOCATION",Latitude+"\n hi"+Longitude);
                            LatLng pointer = new LatLng( Latitude, Longitude);
                            mOrigin = pointer;
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pointer,3.0f));
                            map.setMyLocationEnabled(true);
                            Toast.makeText(PoliceLocation.this, "Latitude:"+Latitude+"\n Longitude"+Longitude, Toast.LENGTH_LONG).show();
                        }
                    }

                });

    }
    public void showDetails( String mId){


        DocumentReference documentReference = db.collection("patrollingUsers").document(mId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("POLICE_LOCATION", "DocumentSnapshot data: " + document.getString("name"));
                        final String Phone = document.getString("phone");
                        String Name = document.getString("name");
                        linearLayout.setVisibility(View.VISIBLE);
                        PoliceExit.setVisibility(View.VISIBLE);
                        policeName.setText(Name);
                        policeNumber.setText(Phone);

                        CallPolice.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (checkSelfPermission(Manifest.permission.CALL_PHONE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(PoliceLocation.this, new String[]{CALL_PHONE}, 200);

                                } else {
                                    String number = "tel:" + Phone;
                                    if (Phone != null) {
                                        Intent intent = new Intent(Intent.ACTION_CALL);
                                        intent.setData(Uri.parse(number));
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(PoliceLocation.this, "You have not uploaded any number", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });


                        SendDetails.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {

                                    String text = "My GroupId is "+groupId+ "and my user ID is "+ userID + " I am in danger Please help me by Tracking my Location";// Replace with your message.

                                    String toNumber = "91" + Phone; // Replace with mobile phone number without +Sign or leading zeros, but with country code
                                    //Suppose your country is India and your phone number is “xxxxxxxxxx”, then you need to send “91xxxxxxxxxx”.


                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
                                    startActivity(intent);
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                        });

                        PoliceExit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                linearLayout.setVisibility(View.INVISIBLE);
                                PoliceExit.setVisibility(View.INVISIBLE);
                            }
                        });



                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }
}
