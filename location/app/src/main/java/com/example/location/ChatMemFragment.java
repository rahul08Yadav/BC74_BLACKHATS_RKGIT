package com.example.location;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.location.MainActivity.groupId;
import static com.example.location.MainActivity.userID;

public class ChatMemFragment extends Fragment {

    private String TAG = "CHAT_MEM_Fragment";
    FirebaseFirestore dba;
    private int ii =0;
    private  ArrayList<String> userList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private  ListView listView;
    TextView gp;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.chat_mem_listview,container,false);
        dba = FirebaseFirestore.getInstance();
        listView = view.findViewById(R.id.chat_mem_list);
        gp = view.findViewById(R.id.gp);
        gp.setText(groupId);
        String[] users = {};
        userList = new ArrayList<>(Arrays.asList(users));
        arrayAdapter = new ArrayAdapter<String>(getContext(),R.layout.members_list,R.id.mem_list_text,userList);
        listView.setAdapter(arrayAdapter);

        userList.clear();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("CHATMEM_FRAGMENT"," "+i);
               if(userList.get(i).equals("ImportantAlerts") ){
                   Log.d("CHATMEM_FRAGMENT"," "+i);
                   Fragment fragment = new AlertMessage();
                   FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                   FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                   fragmentTransaction.replace(R.id.fragment_container, fragment);
                   fragmentTransaction.addToBackStack(null);
                   fragmentTransaction.commit();
               }else {
                   String otherPerson = userList.get(i);
                   Fragment fragment = new ChatsFragment();
                   Bundle args = new Bundle();
                   args.putString("otherPersonId", otherPerson);
                   fragment.setArguments(args);
                   FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                   FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                   fragmentTransaction.replace(R.id.fragment_container, fragment);
                   fragmentTransaction.addToBackStack(null);
                   fragmentTransaction.commit();
               }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        dba.collection("chats").document(groupId).collection("users")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for(QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots)
                {
                    Log.d(TAG,"subject Success" + userID +"  "+ documentSnapshot.getId());
                    if(ii == 0){
                        if(documentSnapshot.getId().equals(userID)){
                          continue;
                        }

                        userList.add(documentSnapshot.getId());
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
                ii = ii +1;
            }
        });
        super.onResume();
    }

}
