package com.example.location;

import android.Manifest;

import android.content.pm.PackageManager;

import android.location.Location;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


import java.util.HashMap;
import java.util.Map;

import static com.example.location.MainActivity.groupId;
import static com.example.location.MainActivity.userID;
import static com.example.location.MainActivity.userName;


public class ProfileFragment extends Fragment {
   private Button joinGrp,createCode,leaveGrpBtn;
   private EditText groupId;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String Longitude, Latitude;
    private TextView textIdReplace,noTextreplace,preTextreplace,preText,noText;

    private  FirebaseFirestore db;

   private String userID;
   private ProgressBar mProgress;
    private String gcode;
    private String TAG = "PROFILE_FRAGMENT";
    private String GroupID;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        joinGrp = view.findViewById(R.id.joinGroup);
        groupId = view.findViewById(R.id.groupId);

        mProgress = view.findViewById(R.id.profileProgress);
        FirebaseAuth fAuth;
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        createCode = view.findViewById(R.id.createCode);
        textIdReplace = view.findViewById(R.id.groupIdreplace);
        leaveGrpBtn = view.findViewById(R.id.leave_grp);
        noTextreplace = view.findViewById(R.id.noTextreplace);
        preTextreplace = view.findViewById(R.id.preTextreplace);
        preText = view.findViewById(R.id.preText);
        noText = view.findViewById(R.id.noText);
        userID = fAuth.getCurrentUser().getEmail();

        ((MainActivity) getActivity()).exit.setVisibility(View.INVISIBLE);
        if(ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());




        DocumentReference documentReference = db.collection("users").document(userID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getString("GroupId") + document.getString("Name"));
                        //ParentsEmail = document.getString("ParentEmail");
                        GroupID = document.getString("GroupId");
                        if(GroupID != null){
                            joinGrp.setEnabled(false);
                            //joinGrp.getBackground().setColorFilter( 0x808080 , PorterDuff.Mode.MULTIPLY);
                            groupId.setVisibility(View.INVISIBLE);
                            createCode.setVisibility(View.INVISIBLE);
                            textIdReplace.setVisibility(View.VISIBLE);
                            textIdReplace.setText(GroupID);
                            leaveGrpBtn.setVisibility(View.VISIBLE);
                            noTextreplace.setVisibility(View.VISIBLE);
                            preTextreplace.setVisibility(View.VISIBLE);
                            preText.setVisibility(View.INVISIBLE);
                            noText.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        joinGrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gcode = groupId.getText().toString();

                if (gcode.length() == 6) {
                    mProgress.setVisibility(View.VISIBLE);
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener( getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Latitude = String.valueOf(location.getLatitude());
                                Longitude = String.valueOf(location.getLongitude());

                                Log.d(TAG, "Location Not Null");
                                Map<String,Object> code = new HashMap<>();
                                code.put("GroupId", gcode);
                                db.collection("users").document(userID).set(code, SetOptions.merge());
                                sendingFusedLocation();
// Logic to handle location object
                            }else{
                                Toast.makeText(getContext(), "Location Null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Please Enter Valid Code", Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.INVISIBLE);
                }



            }
        });

        createCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment fragment = new CreateCodeFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        leaveGrpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("Location").document(GroupID).collection("users").document(userID)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                Map<String,Object> code = new HashMap<>();
                                code.put("GroupId", null);

                                db.collection("users").document(userID).set(code, SetOptions.merge());

                                joinGrp.setEnabled(true);
                                //joinGrp.getBackground().setColorFilter( 0x808080 , PorterDuff.Mode.MULTIPLY);
                                groupId.setVisibility(View.VISIBLE);
                                createCode.setVisibility(View.VISIBLE);
                                textIdReplace.setVisibility(View.INVISIBLE);
                                leaveGrpBtn.setVisibility(View.INVISIBLE);
                                noTextreplace.setVisibility(View.INVISIBLE);
                                preTextreplace.setVisibility(View.INVISIBLE);
                                preText.setVisibility(View.VISIBLE);
                                noText.setVisibility(View.VISIBLE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });


            }
        });


        return view;
    }





    public void sendingFusedLocation() {
        Toast.makeText(getContext(), "Group joined", Toast.LENGTH_SHORT).show();
        DocumentReference documentReference = db.collection("Location").document(gcode).collection("users").document(userID);
        Map<String,Object> user = new HashMap<>();
        user.put("latitude",String.valueOf(Latitude));
        user.put("longitude",String.valueOf(Longitude));
        Log.d("LOCATION",String.valueOf(Latitude) + '+' + Longitude);

        Map<String,Object> code = new HashMap<>();
        code.put( "Text" , " ");
        db.collection("chats").document(gcode).collection("users").document(userID).set(code);

        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgress.setVisibility(View.INVISIBLE);
                Fragment fragment = new DashboardFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                Log.d("TAG", "onSuccess: location stored "+ userID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: " + e.toString());
            }
        });



    }

}
