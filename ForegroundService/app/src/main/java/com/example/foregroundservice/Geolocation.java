package com.example.foregroundservice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import static com.example.foregroundservice.MainActivity.userCode;
import static com.example.foregroundservice.MainActivity.userEmail;

public class Geolocation extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Double Latitude, Longitude;
    private LatLng mOrigin;
    Marker marker;
    FirebaseFirestore db;
    String code,mail;
    private String TAG = "GEOLOCATION";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geo_fencing);
        db = FirebaseFirestore.getInstance();
        mail = getIntent().getStringExtra("userEmail");
        code = getIntent().getStringExtra("userCode");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Geolocation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);

            ActivityCompat.requestPermissions(Geolocation.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);

            return;
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(Geolocation.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gettingFusedLocation();

        DocumentReference docRef = db.collection("Location").document(userCode).collection("users").document(userEmail);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
//                    if(marker != null){
//                        marker.remove();
//                    }
                    Log.d("TAG", "Current data1: " + snapshot.get("longitude"));
                    Log.d("TAG", "Current data1: " + snapshot.get("latitude"));
                    String latti1 = String.valueOf(snapshot.get("latitude"));
                    String longi1 = String.valueOf(snapshot.get("longitude"));
                    LatLng sydney = new LatLng(Double.parseDouble(latti1), Double.parseDouble(longi1));
                    mMap.addMarker(new MarkerOptions().position(sydney).title(userCode).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).snippet(userEmail));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latti1), Double.parseDouble(longi1)), 15.0f));

                    // animateMarker(marker,sydney,true);


                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });



    }
    public void gettingFusedLocation(){
        fusedLocationClient.getLastLocation()
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
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pointer,15.0f));
                            mMap.setMyLocationEnabled(true);
                            Toast.makeText(Geolocation.this, "Latitude:"+Latitude+"\n Longitude"+Longitude, Toast.LENGTH_LONG).show();
                        }
                    }

                });

    }

    @Override
    protected void onStart() {
        super.onStart();


    }
}
