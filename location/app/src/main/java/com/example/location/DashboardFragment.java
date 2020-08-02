package com.example.location;

import android.Manifest;

import android.app.ActivityManager;

import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.location.Location;

import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;


import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.SEND_SMS;
import static android.app.Activity.RESULT_OK;
import static com.example.location.MainActivity.groupId;
import static com.example.location.MainActivity.parentPhone;
import static com.example.location.MainActivity.userName;


public class DashboardFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Button mSendAlerts, mSendMail, mRecordVideo, mwhatsapp, callBtn, msgBtn, grpChat, policeMap;
    // TextView textView1, textView2, textView3, textView4, textView5;
    FirebaseAuth fAuth;
    public static byte[] byte_data;
    FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseFirestore db;
    String userID, ParentsEmail, phoneNumber;
    ProgressBar mProgress;
    ImageView rightWifi, leftWifi;
    public String UserName;
    String img_url;
    private String  Latitude, Longitude;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    Timer timer;
    String TAG = "TAG";
    public static List<String> allPhoneNumber = new ArrayList<String>();

    public static String groupIdd;


    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private StorageReference mStorageRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
mStorageRef = FirebaseStorage.getInstance().getReference();

        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        mSendAlerts = view.findViewById(R.id.send_alerts);
//        textView1 = view.findViewById(R.id.text_view1);
//        textView2 = view.findViewById(R.id.text_view2);
//        textView3 = view.findViewById(R.id.text_view3);
//        textView4 = view.findViewById(R.id.text_view4);
//        textView5 = view.findViewById(R.id.text_view5);
        mSendMail = view.findViewById(R.id.fetch);
        mwhatsapp = view.findViewById(R.id.whatsAppBtn);
        mRecordVideo = view.findViewById(R.id.record_video);
        mProgress = view.findViewById(R.id.dashProgress);
        callBtn = view.findViewById(R.id.callBtn);
        msgBtn = view.findViewById(R.id.SendMessageBtn);
        grpChat = view.findViewById(R.id.grp_chat);
        rightWifi = view.findViewById(R.id.right_wifi);
        leftWifi = view.findViewById(R.id.left_wifi);
        policeMap = view.findViewById(R.id.fetch);

        ((MainActivity) getActivity()).mdrawerBtn.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).exit.setVisibility(View.VISIBLE);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());


        userID = fAuth.getCurrentUser().getEmail();


        DocumentReference documentReference = db.collection("users").document(userID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getString("ParentEmail"));
                        phoneNumber = document.getString("phone");
                        ParentsEmail = document.getString("ParentEmail");
                        UserName = document.getString("Name");
                        groupIdd = document.getString("GroupId");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        policeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent police = new Intent(getActivity(),PoliceLocation.class);
                startActivity(police);
            }
        });

        grpChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ChatMemFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        msgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{SEND_SMS},
                            1
                    );
                } else {
                    mProgress.setVisibility(View.VISIBLE);
                    sendSms();
                }

            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(),CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{CALL_PHONE},
                            1
                    );
                } else {
                    String number = "tel:" + phoneNumber;
                    if (phoneNumber != null) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse(number));
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "You have not uploaded any number", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

//        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
//
//            locationListener = new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    double longitude = location.getLongitude();
//                    double lattitude = location.getLatitude();
//
//                    try{
//                        Geocoder geocoder = new Geocoder(getContext(),Locale.getDefault());
//                        List<Address> addressList = geocoder.getFromLocation(lattitude,longitude,1);
//
//                        textView1.setText("Latitude: " + addressList.get(0).getLatitude());
//                        textView2.setText("Longitude: " + addressList.get(0).getLongitude());
//                        textView3.setText("Country: " + addressList.get(0).getCountryName());
//                        textView4.setText("Locality: " + addressList.get(0).getSubLocality());
//                        textView5.setText("Address: " + addressList.get(0).getAddressLine(0));
//
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }
//
//                    userID = fAuth.getCurrentUser().getEmail();
//
//
//
//                    DocumentReference documentReference = db.collection("Location").document(groupIdd).collection("users").document(userID);
//                    Map<String,Object> user = new HashMap<>();
//                    user.put("latitude",String.valueOf(lattitude));
//                    user.put("longitude",String.valueOf(longitude));
//
//                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//
//                            Log.d("TAG", "onSuccess: location stored "+ userID);
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.d("TAG", "onFailure: " + e.toString());
//                        }
//                    });
//                }
//
//                @Override
//                public void onStatusChanged(String s, int i, Bundle bundle) {
//
//                }
//
//                @Override
//                public void onProviderEnabled(String s) {
//
//                }
//
//                @Override
//                public void onProviderDisabled(String s) {
//
//                }
//            };


        mwhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    Fragment fragment = new MessageFragment();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        mSendAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                    rightWifi.setVisibility(View.INVISIBLE);
                    leftWifi.setVisibility(View.INVISIBLE);
                    rightWifi.clearAnimation();
                    leftWifi.clearAnimation();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentDateTime = dateFormat.format(new Date());
                    String cdate = currentDateTime.substring(0,11);
                    String ctime = currentDateTime.substring(11,19);

                    Log.d("ChatsFragment", "Current Timestamp: " + currentDateTime.substring(11,19) + " "+currentDateTime.substring(0,11));
                    String putText = userName + " has stopped his journey at " + ctime + " on " + cdate + " you can view " + userName +
                            " last location in the maps";

                    Map<String,Object> code = new HashMap<>();
                    String key =  cdate + " " + userName + " " + ctime;
                    code.put( key , putText);
                    db.collection("chats").document(groupId).collection("users").document("ImportantAlerts").set(code, SetOptions.merge());


                    Log.d("TAG", "timer:" + timer);
                    stopLocationServices();
                    return;
                }
                timer = new Timer();
                blinking();



                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateTime = dateFormat.format(new Date());
                String cdate = currentDateTime.substring(0,11);
                String ctime = currentDateTime.substring(11,19);

                Log.d("ChatsFragment", "Current Timestamp: " + currentDateTime.substring(11,19) + " "+currentDateTime.substring(0,11));
                String putText = userName + " has started his journey at " + ctime + " on " + cdate + " you can view " + userName +
                        " live location in the maps";

                        Map<String,Object> code = new HashMap<>();
                        String key =  cdate + " " + userName + " " + ctime;
                        code.put( key , putText);
                        db.collection("chats").document(groupId).collection("users").document("ImportantAlerts").set(code, SetOptions.merge());


                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION);
                    rightWifi.setVisibility(View.INVISIBLE);
                    leftWifi.setVisibility(View.INVISIBLE);
                    rightWifi.clearAnimation();
                    leftWifi.clearAnimation();
                } else {
                    startLocationService();
                }
            }
        });


//        mSendMail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        // Got last known location. In some rare situations this can be null.
//                        if (location != null) {
//                            Latitude = String.valueOf(location.getLatitude());
//                            Longitude = String.valueOf(location.getLongitude());
//                            URL = "https://www.google.com/maps/search/?api=1&query=" + Latitude + "," + Longitude;
//                            sendMail();
//                            Toast.makeText(getContext(), "Sending Mail To:- " + ParentsEmail, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//            }
//        });

        mRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    //main logic or main code
                    dispatchTakeVideoIntent();
                    // . write your main code to execute, It will execute if the permission is already given.

                } else {
                    requestPermissions(
                            new String[]{Manifest.permission.CAMERA},
                            1);
                }

            }
        });


        return view;
    }

//    private void dispatchTakeVideoIntent() {
//        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
//        }

        private void dispatchTakeVideoIntent() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_VIDEO_CAPTURE);
            }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Uri content_uri = data.getData();
            Log.d("Photo", "photo" + content_uri);
            Bitmap image = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte_data = baos.toByteArray();
            uploadProfileImageToFirebase(userID,byte_data);

        }
    }
    private void uploadProfileImageToFirebase(String Email,byte[] content_data) {
        mProgress.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "uri" + content_data
                , Toast.LENGTH_LONG).show();
        StorageReference imgRef = mStorageRef.child("Videos").child(Email);
        imgRef.putBytes(content_data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(getContext(), "Upload success"
                                , Toast.LENGTH_LONG).show();
                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uri.isComplete()) ;
                        Uri url = uri.getResult();
                        img_url = url.toString();
                        Map<String,Object> data = new HashMap<>();
                        data.put("image_url", img_url);

                        db.collection("users").document(userID).set(data, SetOptions.merge());


                      mProgress.setVisibility(View.INVISIBLE);// Get a URL to the uploaded content
                        Log.d("URL", "Download URL is: " + url);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(getContext(), "Upload Unsuccessful"
                                , Toast.LENGTH_LONG).show();
                    }
                });
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//        if(ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10,0,locationListener);
//            textView1.setText("Please keep Gps On \n" + "Getting Location");
//            }else{
//                Toast.makeText(getContext(), "Turn on GPS For Accurate Location", Toast.LENGTH_SHORT).show();
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,10,0,locationListener);
//            }
//
//        }
//    }else{
//        Toast.makeText(getContext(),"Location Access Not Provided",Toast.LENGTH_SHORT).show();
//    }
//
//
//    }



    //    public void blinking(){
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                rightWifi.setVisibility(View.VISIBLE);
//                leftWifi.setVisibility(View.VISIBLE);
//            }
//        },2000);
//    }
    public void blinking() {

        Animation animation1 =
                AnimationUtils.loadAnimation(getContext(),
                        R.anim.blink);
        rightWifi.startAnimation(animation1);
        leftWifi.startAnimation(animation1);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startLocationService();
//                blinking();
//            } else {
//                Toast.makeText(getContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
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
            Intent intent = new Intent(getContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            getActivity().startService(intent);
            Toast.makeText(getContext(), "Location Service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationServices() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERRVICE);
            getActivity().startService(intent);
            Toast.makeText(getContext(), "Location Service stopped", Toast.LENGTH_SHORT).show();
        }
    }
//    public void sendMail() {
//        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                // Got last known location. In some rare situations this can be null.
//                if (location != null) {
//                    Latitude = String.valueOf(location.getLatitude());
//                    Longitude = String.valueOf(location.getLongitude());
//                    URL = "https://www.google.com/maps/search/?api=1&query=" + Latitude + "," + Longitude;
//                    Toast.makeText(getContext(), "Sending Mail To:- " + ParentsEmail, Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//
//    }

    private void sendSms() {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {

                    Latitude = String.valueOf(location.getLatitude());
                    Longitude = String.valueOf(location.getLongitude());
                    String URLW = "https://www.google.com/maps/search/?api=1&query=" + Latitude + "," + Longitude;

                    final String text = "Alert!! your family member " + UserName + " has sent an alert.\n"
                            + " his location is:-\n" + URLW;

//                    String mail = ParentsEmail;
//                    String message = "Alert!! your family member " + UserName + " is in Danger and has sent you a request to help.\n"
//                            + UserName + " has sent the location for you:-\n" + URLW + "\n" + "Please login to our website for more information and help " + UserName;
//                    String subject = UserName + " is asking for Help!!";
//                    JavaMailAPI javaMailAPI = new JavaMailAPI(getContext(), mail, subject, message);
//                    javaMailAPI.execute();
//
//                    Log.d("URL", URLW);

                    for (int i = 0; i < allPhoneNumber.size(); i++) {

                        String mobileNumber = allPhoneNumber.get(i);

                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(mobileNumber, null, text, null, null);
                        Log.d("DASHBOARD_FRAGMENT", "" + allPhoneNumber.get(i));
                    }

                    mProgress.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Messages Sent", Toast.LENGTH_SHORT).show();
//                            Intent i = new Intent(Intent.ACTION_SENDTO);
//                            i.putExtra("address", allPhoneNumber);
//                            i.putExtra("sms_body", text);
//                            i.setType("vnd.android-dir/mms-sms");
//                            startActivity(i);

                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        allPhoneNumber.clear();
        allPhoneNumber.add(parentPhone);
        DocumentReference phoneRef = db.collection("contact").document(userID);
        phoneRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        for (Map.Entry<String, Object> set : document.getData().entrySet()) {
                            String name = set.getKey();
                            String phone = set.getValue().toString();
                            allPhoneNumber.add(phone);


                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }



}