package com.example.location;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.content.CursorLoader;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.grpc.internal.SharedResourceHolder;

public class MessageFragment extends Fragment {
    Button addContactBtn,addMembersBtn,exit;
    String TAG = "TAG";
    TextView addManually,save;
    LinearLayout display2,display1;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    String userId;
    private ArrayList <String> arrayList ;
    private ArrayAdapter <String> arrayAdapter;
    EditText contactName,contactNumber;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_list,container,false);
        addContactBtn = view.findViewById(R.id.addContacts);
        addMembersBtn = view.findViewById(R.id.addMembers);
        display2 = view.findViewById(R.id.from_contact_layout);
        display1 = view.findViewById(R.id.manual_add_layout);
        addManually = view.findViewById(R.id.add_contact_manually);
        contactName = view.findViewById(R.id.contactName);
        contactNumber = view.findViewById(R.id.contactNumber);
        exit = view.findViewById(R.id.exit);
        db =FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        userId = fAuth.getCurrentUser().getEmail();

        final ListView listView = view.findViewById(R.id.listViewContacts);
        save = view.findViewById(R.id.save);
        String[] itemPhone = {};
        arrayList = new ArrayList<>(Arrays.asList(itemPhone));
        arrayAdapter = new ArrayAdapter<String>(getContext(),R.layout.list_item,R.id.textItem2,arrayList);
        listView.setAdapter(arrayAdapter);



        DocumentReference documentReference = db.collection("contact").document(userId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        for (Map.Entry<String, Object> set : document.getData().entrySet()) {
                            String name = set.getKey();
                            String phone = set.getValue().toString();
                            arrayList.add(name);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display1.setVisibility(View.INVISIBLE);
                display2.setVisibility(View.INVISIBLE);
                exit.setVisibility(View.INVISIBLE);

            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = contactNumber.getText().toString();
                String name = contactName.getText().toString();
                if(number.length()!=10){
                    contactNumber.setError("Phone Number is Invalid");
                    return;
                }else if(TextUtils.isEmpty(name)){
                    contactName.setError("Enter Name");
                    return;
                }else{
                    arrayList.add(number);
                    arrayAdapter.notifyDataSetChanged();
                    display1.setVisibility(View.INVISIBLE);
                    exit.setVisibility(View.INVISIBLE);
                    Map<String,Object> code = new HashMap<>();
                    code.put(name, number);

                    db.collection("contact").document(userId).set(code, SetOptions.merge());
                }


            }
        });
        addManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display2.setVisibility(View.INVISIBLE);
                display1.setVisibility(View.VISIBLE);
                contactName.getText().clear();
                contactNumber.getText().clear();
            }
        });
        addMembersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display2.setVisibility(View.VISIBLE);
                exit.setVisibility(View.VISIBLE);
            }
        });
        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              sendMessage();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String phoneNumber = (listView.getItemAtPosition(i).toString());
                String number = "tel:" + phoneNumber;
                if(phoneNumber!=null) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(number));
                    startActivity(intent);
                }else{
                    Toast.makeText(getContext(),"Error! Try Again",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    public void sendMessage(){
        Intent intent= new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent,1);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult()");
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contactsData = data.getData();
                CursorLoader loader = new CursorLoader(getContext(), contactsData, null, null, null, null);
                Cursor c = loader.loadInBackground();
                if (c.moveToFirst()) {
                    Log.i(TAG, "Contacts ID: " + c.getString(c.getColumnIndex(ContactsContract.Contacts._ID)));
                    Log.i(TAG, "Contacts Name: " + c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    Log.i(TAG, "Contacts Phone Number: " + c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                }
            }
        }
    }
}
