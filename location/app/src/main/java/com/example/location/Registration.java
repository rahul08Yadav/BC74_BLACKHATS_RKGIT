package com.example.location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity implements View.OnClickListener {

    EditText mEmail, mPassword, mPhone, pEmail, userName;
    Button mRegistrationBtn;
    Button muserPicBtn;
    TextView mLoginBtn,regText;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    String userID;
    StorageReference storageReference;
    ImageView img;
    String img_url;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static byte[] byte_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_page);
        mEmail = findViewById(R.id.registeremail);
        mPassword = findViewById(R.id.registerpassword);
        mPhone = findViewById(R.id.phone);
        pEmail = findViewById(R.id.parentsEmail);
        userName = findViewById(R.id.userName);
        mRegistrationBtn = findViewById(R.id.registerbutton);
       regText = findViewById(R.id.regText);
        progressBar = findViewById(R.id.authProgress);
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        img = findViewById(R.id.userImg);

        storageReference = FirebaseStorage.getInstance().getReference();

        muserPicBtn = findViewById(R.id.userPicUpload);
        mRegistrationBtn.setOnClickListener(Registration.this);


        muserPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(Registration.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {

                    dispatchTakePictureIntent();
                    Toast.makeText(Registration.this, "pressed", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "button pressed");
                } else {
                    ActivityCompat.requestPermissions(Registration.this, new String[]{Manifest.permission.CAMERA}, 1);
                }

            }
        });

//


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerbutton:
                final String regt = regText.getText().toString().trim();
                final String name = userName.getText().toString().trim();
                final String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                final String parentEmail = pEmail.getText().toString();
                final String phone = mPhone.getText().toString();

                if (TextUtils.isEmpty(regt)){
                    muserPicBtn.setError("Photo is Required");
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required");
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    userName.setError("Name is Required");
                    return;
                }
                if (TextUtils.isEmpty(parentEmail)) {
                    pEmail.setError("Parents Email is Required");
                    return;
                }
                if (TextUtils.isEmpty(phone)) {
                    mPhone.setError("Phone.no is Required");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password Must be more than 6 character");
                    return;
                }
                if (phone.length() < 10) {
                    mPhone.setError("Phone Number is Invalid");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);


                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            Toast.makeText(getApplicationContext(), "User Created", Toast.LENGTH_SHORT).show();


                            userID = fAuth.getCurrentUser().getEmail();
                            uploadProfileImageToFirebase(email,byte_data);
                            DocumentReference documentReference = db.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("Name", name);
                            user.put("ParentEmail", parentEmail);
                            user.put("email", email);
                            user.put("phone", phone);
                            user.put("password", password);
                            user.put("GroupId",null);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Log.d("TAG", "onSuccess: user Profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG", "onFailure: " + e.toString());
                                }
                            });
                            Intent i = new Intent(Registration.this, MainActivity.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(Registration.this, "Error!!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
                break;


//            case R.id.regToLog:
//                Toast.makeText(getApplicationContext(), "sign in pressed", Toast.LENGTH_SHORT).show();
//                Intent i = new Intent(Registration.this, Login.class);
//                startActivity(i);
//                break;


        }
    }

    private void uploadProfileImageToFirebase(String Email,byte[] content_data) {
        Toast.makeText(Registration.this, "uri" + content_data
                , Toast.LENGTH_LONG).show();
        StorageReference imgRef = storageReference.child("profile_photos").child(Email);
        imgRef.putBytes(content_data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(Registration.this, "Upload success"
                                , Toast.LENGTH_LONG).show();
                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uri.isComplete()) ;
                        Uri url = uri.getResult();
                         img_url = url.toString();
                      Map<String,Object> data = new HashMap<>();
                      data.put("image_url", img_url);

                      db.collection("users").document(userID).set(data, SetOptions.merge());



                        Log.d("URL", "Download URL is: " + url);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(Registration.this, "Upload Unsuccessful"
                                , Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
        takePictureIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            Log.d("TAG", "opening Camera");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Uri content_uri = data.getData();
            Log.d("Photo", "photo" + content_uri);
            Bitmap image = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte_data = baos.toByteArray();
           regText.setText("Image Uploaded");
            img.setImageBitmap(image);

        }
    }


}
