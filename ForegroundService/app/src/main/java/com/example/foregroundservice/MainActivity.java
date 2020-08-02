package com.example.foregroundservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    Button btnStartService, btnStopService ,button,signOut;
    EditText code,email;
    FirebaseAuth fAuth;
    public static String userId;
    public static String userCode,userEmail;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        code = findViewById(R.id.groupid);
        email = findViewById(R.id.email);
        btnStartService = findViewById(R.id.buttonStartService);
        btnStopService = findViewById(R.id.buttonStopService);
        button = findViewById(R.id.checkLocation);
        signOut = findViewById(R.id.signOut);
        fAuth = FirebaseAuth.getInstance();

        userId = fAuth.getCurrentUser().getEmail();

         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 userCode = code.getText().toString().trim();
                 userEmail = email.getText().toString().trim();
                 if (TextUtils.isEmpty(userCode)) {
                     code.setError("GroupId is required");
                     return;
                 }
                 if (TextUtils.isEmpty(userEmail)) {
                     email.setError("Email is required");
                     return;
                 }
                 if (userCode.length() != 6) {
                     code.setError("Code should be 6 digit");
                     return;
                 }else {
                     Intent i = new Intent(MainActivity.this, Geolocation.class);
                     i.putExtra(userCode,"userCode");
                     i.putExtra(userEmail,"userEmail");
                     startActivity(i);
                 }
             }
         });

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    startLocationService();
                }
            }
        });

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationServices();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fAuth.getInstance().signOut();
                SharedPreferences preferences = getSharedPreferences("sharedPrefs", 0);
                preferences.edit().remove("text").commit();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location Service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationServices() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERRVICE);
            startService(intent);
            Toast.makeText(this, "Location Service stopped", Toast.LENGTH_SHORT).show();
        }
    }


}

