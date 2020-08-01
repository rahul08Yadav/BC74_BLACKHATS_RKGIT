package com.example.location;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TextView textView;
    LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    VideoView videoView;
    FirebaseFirestore db;
    Double latitude,longitude;
    Button mSendAlerts,mSendMail,mRecordVideo;
    Timer timer;
    FirebaseAuth fAuth;
    public Button mdrawerBtn;

    private StorageReference mStorage;
    public static String userID, userName,parentEmail,parentPhone;
    public static String groupId ;
    public static String  imgURl;

    String text ,TAG = "TAG";


    private MediaRecorder recorder;
    private ProgressDialog mProgress;
    private final String CHANNEL_ID = "personal_notification";
    private final int NOTIFICATION_ID = 001;

    View hView;
    private  DrawerLayout drawer;
    private ActionBarDrawerToggle mToggle;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mdrawerBtn = findViewById(R.id.navigationDrawer);
        mdrawerBtn.setVisibility(View.VISIBLE);
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        mdrawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
//        mToggle = new ActionBarDrawerToggle(this,drawer,R.string.open,R.string.close);
//        drawer.addDrawerListener(mToggle);
//        mToggle.syncState();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = FirebaseFirestore.getInstance();
        final NavigationView navigationView = findViewById(R.id.nv);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();
        fAuth= FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getEmail();
        mStorage = FirebaseStorage.getInstance().getReference();






        DocumentReference documentReference = db.collection("users").document(userID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getString("ParentEmail"));
                        groupId = document.getString("GroupId");
                        userName = document.getString("Name");
                        imgURl = document.getString("image_url");
                        parentEmail = document.getString("ParentEmail");
                        parentPhone = document.getString("phone");

                        if(savedInstanceState == null){
                            if(groupId == null){
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                                navigationView.setCheckedItem(R.id.profile);
                                mdrawerBtn.setVisibility(View.INVISIBLE);
                            }
                            else {
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
                                navigationView.setCheckedItem(R.id.dashboard);
                            }
                        }

                        if(imgURl == null){
                            Drawable res = getResources().getDrawable(R.drawable.alert);
                            ImageView img_user = hView.findViewById(R.id.profilePic);
                            img_user.setImageDrawable(res);
                        }else{
                            ImageView img_user = hView.findViewById(R.id.profilePic);
                            Log.d("TAG","Image URl is" + imgURl);
                            Picasso.get().load(imgURl).into(img_user);
                        }
                        TextView nav_name = hView.findViewById(R.id.fauthname);
                        nav_name.setText(userName);
                        Log.d(TAG, "userName" + userName);



                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        hView =  navigationView.getHeaderView(0);
        TextView nav_user = hView.findViewById(R.id.email);
        nav_user.setText(userID);



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);








        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        text = sharedPreferences.getString(TEXT, "");
        mProgress = new ProgressDialog(this);




    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.dashboard:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new DashboardFragment()).commit();
                drawer.closeDrawers();
                break;
            case R.id.video_self_defence:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new VideoTipsFragment()).commit();
                drawer.closeDrawers();
                break;

            case R.id.geofence:
                Intent i= new Intent(MainActivity.this,GeoFencing.class);
                drawer.closeDrawers();
                startActivity(i);
                break;
            case R.id.profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
                drawer.closeDrawers();

                break;
            case R.id.emergency_contact:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new UserProfileUI()).commit();
                drawer.closeDrawers();
                break;

            case R.id.tips_for_citizen:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new TipsFragment()).commit();
                drawer.closeDrawers();

                break;
            case R.id.about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new AboutAppFragment()).commit();
                drawer.closeDrawers();
                break;
            case R.id.logout:
                fAuth.getInstance().signOut();
                SharedPreferences preferences = getSharedPreferences("sharedPrefs", 0);
                preferences.edit().remove("text").commit();
                Intent intent = new Intent(MainActivity.this,Login.class);
                startActivity(intent);
                break;
        }
        return true;
    }


    boolean twice = false;
    @Override
    public void onBackPressed() {

        Log.d("TAG","click");
        if(twice==true){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags((Intent.FLAG_ACTIVITY_CLEAR_TOP));
            startActivity(intent);
            finish();
            System.exit(0);
        }
        twice = true;
        Log.d("TAG","twice:" + twice);

        Toast.makeText(getApplicationContext(),"press again to exit",Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                twice = false;
                Log.d("TAG","twice" + twice);
            }
        },3000);

    }

}