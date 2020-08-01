package com.example.location;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import static com.example.location.MainActivity.groupId;
import static com.example.location.MainActivity.userID;
import static com.example.location.MainActivity.userName;
import static com.example.location.R.drawable.chatbox_other_design;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    Button sendChat;
    private ArrayList<String> mNames = new ArrayList<>();
    EditText chatText;
    TextView Group;
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    private ArrayList <String> arrayList ;
    private ArrayAdapter<String> arrayAdapter;
    private String otherPerson;

    String TAG = "CHAT FRAGMENT";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_chat_view,container,false);

         otherPerson = getArguments().getString("otherPersonId");

         chatText = view.findViewById(R.id.sendText);
         sendChat = view.findViewById(R.id.sendchat);
         Group = view.findViewById(R.id.g);
         db = FirebaseFirestore.getInstance();
        final ListView listView = view.findViewById(R.id.chatRecycle);
        String[] message = {};
        arrayList = new ArrayList<>(Arrays.asList(message));
        arrayAdapter = new ArrayAdapter<String>(getContext(),R.layout.layout_listner,R.id.myText,arrayList);
        listView.setAdapter(arrayAdapter);
        Group.setText(groupId);



//        DocumentReference documentReference = db.collection("chats").document(groupId);
//        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//
//                        for (Map.Entry<String, Object> set : document.getData().entrySet()) {
//                            String chat = set.getValue().toString();
//                            String user = set.getKey().toString();
//                            arrayList.add(chat);
//                            Log.d("CHATS_FRAGMENT","user: "+ user);
//                            Log.d("CHATS_FRAGMENT","msg: "+ chat);
//                            String[] arrsplit = user.split(" ",5);
//                            Log.d("CHATS_FRAGMENT","arr: "+arrsplit[2]);
//                            if(arrsplit[2] != userName){
//                               Log.d("CHATS_FRAGMENT",arrsplit[2]);
//                            }
//                            arrayAdapter.notifyDataSetChanged();
//                        }
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });


        final DocumentReference docRef = db.collection("chats").document(groupId).collection("users").document(otherPerson);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    String msg = snapshot.getString("Text");
                    arrayList.add(msg);
                    arrayAdapter.notifyDataSetChanged();
//                    arrayList.add(msg);
//                    arrayAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


         sendChat.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                 String currentDateTime = dateFormat.format(new Date());
                 String cdate = currentDateTime.substring(0,11);
                 String ctime = currentDateTime.substring(11,19);

                 Log.d("ChatsFragment", "Current Timestamp: " + currentDateTime.substring(11,19) + " "+currentDateTime.substring(0,11));
                 String putText = chatText.getText().toString();
                 arrayList.add(putText);

                 chatText.getText().clear();
                 Map<String,Object> code = new HashMap<>();
                 String key =  cdate + " " + userName + " " + ctime;
                 code.put( "Text" , putText);
                 db.collection("chats").document(groupId).collection("users").document(userID).set(code); //, SetOptions.merge()
             }
         });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}
