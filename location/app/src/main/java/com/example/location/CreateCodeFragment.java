package com.example.location;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateCodeFragment extends Fragment {
    TextView codeText,doneSharing;
    Button mShareCode,mJoinBtn;
    FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseAuth firebaseAuth;
    String userId;
    ProgressBar mProgress;
    private String Longitude, Latitude;
    FirebaseFirestore db;
    String gcode1;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_generate_code,container,false);
        final String randomCode = getRandomString(6);
        codeText = view.findViewById(R.id.codeText);
        mShareCode = view.findViewById(R.id.shareCode);
        doneSharing = view.findViewById(R.id.doneSharing);
        mJoinBtn = view.findViewById(R.id.join);
        codeText.setText(randomCode);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mProgress = view.findViewById(R.id.genCodeProgress);
        userId = firebaseAuth.getCurrentUser().getEmail();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        mShareCode.setEnabled(false);

        mJoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gcode1 = codeText.getText().toString();
               // textId.setText(groupId.getText().toString());

                if (gcode1.length() == 6) {
                    mProgress.setVisibility(View.VISIBLE);
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener( getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Latitude = String.valueOf(location.getLatitude());
                                Longitude = String.valueOf(location.getLongitude());
                                Map<String,Object> code = new HashMap<>();
                                code.put("GroupId", gcode1);

                                db.collection("users").document(userId).set(code, SetOptions.merge());
                                sendingFusedLocationGen();
// Logic to handle location object
                            }
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Please Enter Valid Code", Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.INVISIBLE);
                }



            }
        });
        mShareCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, "My GroupId is "+randomCode+" join my Crime Related Alerts Group");
                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                   Toast.makeText(getContext(),"Whatsapp have not been installed.",Toast.LENGTH_SHORT).show();
                }

            }
        });

        doneSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new DashboardFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

public static String getRandomString(int i){
    final String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    StringBuilder salt = new StringBuilder();
    while (i > 0) {
        Random rnd = new Random();

        salt.append(SALTCHARS.charAt(rnd.nextInt(SALTCHARS.length())));
        i--;
    }
    String saltStr = salt.toString();
    return saltStr;
}

public void sendingFusedLocationGen(){
    Toast.makeText(getContext(), "Group joined", Toast.LENGTH_SHORT).show();
    DocumentReference documentReference = db.collection("Location").document(gcode1).collection("users").document(userId);
    Map<String,Object> user = new HashMap<>();
    user.put("latitude",String.valueOf(Latitude));
    user.put("longitude",String.valueOf(Longitude));
    Log.d("LOCATION",String.valueOf(Latitude) + '+' + Longitude);

    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            mProgress.setVisibility(View.INVISIBLE);
            mShareCode.setEnabled(true);
            mJoinBtn.setText("Joined");
            mJoinBtn.setEnabled(false);
            doneSharing.setVisibility(View.VISIBLE);
            Log.d("TAG", "onSuccess: location stored "+ userId);
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.d("TAG", "onFailure: " + e.toString());
        }
    });

    Map<String,Object> code = new HashMap<>();
    code.put( "Text" , "Your Important Alerts");
    db.collection("chats").document(gcode1).collection("users").document("ImportantAlerts").set(code);
}
}
