package com.example.location;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.example.location.MainActivity.groupId;
import static com.example.location.MainActivity.userName;

public class AlertMessage extends Fragment {
    TextView alerText;
    private ArrayList<String> arrayList1 ;
    FirebaseFirestore db;

    private String TAG = "ALERT_MESSAGE";
    private ArrayAdapter<String> arrayAdapter1;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.important_alerts,container,false);
        alerText =  view.findViewById(R.id.ta);
        db = FirebaseFirestore.getInstance();
        alerText.setText(groupId);
        final ListView listView = view.findViewById(R.id.alertList);
        String[] message = {};
        arrayList1 = new ArrayList<>(Arrays.asList(message));
        arrayAdapter1 = new ArrayAdapter<String>(getContext(),R.layout.layout_listner1,R.id.myText1,arrayList1);
        listView.setAdapter(arrayAdapter1);

        DocumentReference documentReference = db.collection("chats").document(groupId).collection("users").document("ImportantAlerts");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        for (Map.Entry<String, Object> set : document.getData().entrySet()) {
                            String chat = set.getValue().toString();
                            String user = set.getKey().toString();
                            arrayList1.add(chat);
                            arrayAdapter1.notifyDataSetChanged();
                            Log.d("CHATS_FRAGMENT","user: "+ user);
                            Log.d("CHATS_FRAGMENT","msg: "+ chat);
//                            String[] arrsplit = user.split(" ",5);
//                            Log.d("CHATS_FRAGMENT","arr: "+arrsplit[2]);
//                            if(arrsplit[2] != userName){
//                                Log.d("CHATS_FRAGMENT",arrsplit[2]);
//                            }

                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        return view;
    }
}
