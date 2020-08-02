package com.example.location;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import static com.example.location.MainActivity.userName;
import static com.example.location.MainActivity.userID;
import static com.example.location.MainActivity.parentEmail;
import static com.example.location.MainActivity.groupId;
import static com.example.location.MainActivity.parentPhone;
import static com.example.location.MainActivity.imgURl;

public class UserProfileUI extends Fragment {
     ImageView userImage;
     TextView name,email,pEmail,Pphone,gpId;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_view,container,false);

        userImage = view.findViewById(R.id.imageview_profile);
        name = view.findViewById(R.id.title);
        email = view.findViewById(R.id.desc);
        gpId = view.findViewById(R.id.Gpid);
        pEmail = view.findViewById(R.id.Pemail);
        Pphone = view.findViewById(R.id.Pphone);

        name.setText(userName);
        email.setText(userID);
        gpId.setText(groupId);
        pEmail.setText(userID);
        Pphone.setText(parentPhone);
        Log.d("TAG","Image URl is" + imgURl);
        Picasso.get().load(imgURl).into(userImage);

        return view;
    }
}
