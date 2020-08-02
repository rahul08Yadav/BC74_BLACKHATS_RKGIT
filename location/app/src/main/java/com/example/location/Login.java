package com.example.location;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Login extends AppCompatActivity implements View.OnClickListener {
    //TextView mloginText;
    EditText mEmail, mPassword;
    Button mSignin;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    TextView ChangingText;
    RelativeLayout changing_img;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_page);
        ChangingText = findViewById(R.id.loginauth);
        changing_img = findViewById(R.id.changing_img);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        String ctime = currentDateTime.substring(11,13);
        if(Integer.parseInt(ctime) >= 12){
            ChangingText.setText("Good Evening");
            changing_img.setBackgroundResource(R.drawable.good_night_img);

        }


       // mloginText = findViewById(R.id.registertext);
        mSignin = findViewById(R.id.signin);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.logprogress);

        fAuth = FirebaseAuth.getInstance();


        mSignin.setOnClickListener(this);
       // mloginText.setOnClickListener(this);

        if (fAuth.getCurrentUser() != null) {
            Intent i = new Intent(Login.this, MainActivity.class);
            startActivity(i);
        }

    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        intent.setFlags((Intent.FLAG_ACTIVITY_CLEAR_TOP));
//        startActivity(intent);
//        finish();
//        System.exit(0);
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signin:
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required.");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithEmail:success");
                            Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(Login.this, MainActivity.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(Login.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                });

                break;
//            case R.id.registertext:
//                Intent i = new Intent(Login.this, Registration.class);
//                startActivity(i);
//                break;
        }
    }

    @Override
    public void onBackPressed() {
       Intent i = new Intent(Login.this,IntroScreen.class);
       startActivity(i);

    }

}
