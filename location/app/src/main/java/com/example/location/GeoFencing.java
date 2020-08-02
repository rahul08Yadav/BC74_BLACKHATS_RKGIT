package com.example.location;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class GeoFencing extends AppCompatActivity implements OnMapReadyCallback{
    Button currentLocation,mRefreshButton,mliveButton;
    private FusedLocationProviderClient fusedLocationClient;
    private Double Latitude, Longitude;
    private GoogleMap mMap;
    private ProgressDialog mProgress;
    String userID;
    FirebaseAuth fAuth;
    private MarkerOptions mMarkerOptions;
    private LatLng mOrigin;
    private LatLng mDestination;
    private Polyline mPolyline;
    TextView textStored;
    String TAG = "TAG";

    Marker marker;
    private static String groupId;
    FirebaseFirestore db ;
    List<DocumentSnapshot> subjects = new ArrayList<>();
    List<String> userList = new ArrayList<>();


    Spinner spinner;

    private int ii = 0;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geo_fencing);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(GeoFencing.this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        currentLocation = findViewById(R.id.currentLocation);
        mProgress = new ProgressDialog(this);

       textStored = findViewById(R.id.textStored);
       mRefreshButton = findViewById(R.id.refreshButton);

        userList.add(0,"Members");

        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getEmail();

        gettingFusedLocation();


    // last time left
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(GeoFencing.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);

            ActivityCompat.requestPermissions(GeoFencing.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);

            return;
        }
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //createLocationRequest;

                gettingFusedLocation();



            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        gettingFusedLocation();


//        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//            @Override
//            public void onMapLongClick(LatLng latLng) {
//                mDestination = latLng;
//                mMap.clear();
//                mMarkerOptions = new MarkerOptions().position(mDestination).title("Destination");
//                mMap.addMarker(mMarkerOptions);
//                if(mOrigin != null && mDestination != null)
//                    drawRoute();
//            }
//        });

        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(13.1139757, 77.6251843))
                .radius(1000)
                .strokeColor(Color.TRANSPARENT)
                .fillColor(0x330000ff));

        Circle circle2 = mMap.addCircle(new CircleOptions()
                .center(new LatLng(13.134789, 77.637311))
                .radius(1000)
                .strokeColor(Color.TRANSPARENT)
                .fillColor(0x220000FF));

        Circle circle1 = mMap.addCircle(new CircleOptions()
                .center(new LatLng(13.112933, 77.607700))
                .radius(1000)
                .strokeColor(Color.TRANSPARENT)
                .fillColor(0x440000ff));


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mDestination = marker.getPosition();
                if(mOrigin != null && mDestination != null)
                    drawRoute();
                return false;
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
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pointer,13.0f));
                            mMap.setMyLocationEnabled(true);
                            Toast.makeText(GeoFencing.this, "Latitude:"+Latitude+"\n Longitude"+Longitude, Toast.LENGTH_LONG).show();
                        }
                    }

                });

    }


    @Override
    protected void onStart() {
        super.onStart();


        spinner =  findViewById(R.id.spinner);

        db = FirebaseFirestore.getInstance();


        DocumentReference documentReference = db.collection("users").document(userID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getString("GroupId") + document.getString("Name"));
                        groupId = document.getString("GroupId");
                        textStored.setText(groupId);
                        Log.d(TAG,"groupID =>"+ groupId);
                        db.collection("Location").document(groupId).collection("users")
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots)
                                {
                                    Log.d(TAG,"subject Success");
                                    if(ii == 0){
                                        subjects.add(documentSnapshot);
                                        userList.add(documentSnapshot.getId());
                                    }
                                }
                                ii = ii +1;
                            }
                        });

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!subjects.isEmpty()){
                    initializeUI();
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(final AdapterView<?> adapterView, View view, int i, long l) {
                            if(adapterView.getItemAtPosition(i).equals("Members")){
                                spinner.setSelection(i);

                            }
                            else{
                               final int a = i;
                                 db = FirebaseFirestore.getInstance();


                                DocumentReference ds = db.collection("Location").document(groupId).collection("users").document(adapterView.getItemAtPosition(i).toString());
                                ds.addSnapshotListener(new EventListener<DocumentSnapshot>() {

                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                        @Nullable FirebaseFirestoreException e) {

                                        if (e != null) {
                                            Log.w("TAG", "Listen failed.", e);
                                            return;
                                        }

                                        if (snapshot != null && snapshot.exists()) {
                                            if(marker != null){
                                                marker.remove();
                                            }
                                                Log.d("TAG", "Current data1: " + snapshot.get("longitude"));
                                                Log.d("TAG", "Current data1: " + snapshot.get("latitude"));
                                                String latti1 = String.valueOf(snapshot.get("latitude"));
                                                String longi1 = String.valueOf(snapshot.get("longitude"));
                                                Geocoder geocoder = new Geocoder(GeoFencing.this, Locale.getDefault());
                                            List<Address> addressList = null;
                                            try {
                                                addressList = geocoder.getFromLocation(Double.parseDouble(latti1),Double.parseDouble(longi1),1);
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }

                                                String asdf = String.valueOf(addressList.get(0).getAddressLine(0));
                                                LatLng sydney = new LatLng(Double.parseDouble(latti1), Double.parseDouble(longi1));
                                                marker = mMap.addMarker(new MarkerOptions().position(sydney).title(asdf).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).snippet(adapterView.getItemAtPosition(a).toString()));
                                                marker.setPosition(sydney);
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latti1), Double.parseDouble(longi1)), 15.0f));

                                               // animateMarker(marker,sydney,true);


                                        } else {
                                            Log.d("TAG", "Current data: null");
                                        }
                                    }

                                });
                            }


                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            Toast.makeText(GeoFencing.this, "nothing selected", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Log.d(TAG,"Subject Empty");
                }
            }
        },4000);

    }

    public void initializeUI() {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,  android.R.layout.simple_spinner_dropdown_item, userList);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }




    private void drawRoute(){

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(mOrigin, mDestination);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Key
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception on download", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }




    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask","DownloadTask : " + data);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(8);
            lineOptions.color(Color.RED);
        }

        // Drawing polyline in the Google Map for the i-th route
        if(lineOptions != null) {
            if(mPolyline != null){
                mPolyline.remove();
            }
            mPolyline = mMap.addPolyline(lineOptions);

        }else
            Toast.makeText(getApplicationContext(),"No route is found", Toast.LENGTH_LONG).show();
    }

    }
}
