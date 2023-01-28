package me.electronicsboy.kwitterandroidapp;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class KwitterChat extends AppCompatActivity {

    private ArrayList<String> snapsortsOfExistingData = new ArrayList<>();
    boolean add = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kwitter_chat);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/rooms/" + TempStorage.get("ROOM"));
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    snapsortsOfExistingData.forEach((e) -> {
                        if(snap.getKey().equals(e)) add = false;
                    });
                    if(add) {
                        snapsortsOfExistingData.add(snap.getKey());
                        TextView dynamicTextView = new TextView(KwitterChat.this);
                        dynamicTextView.setLayoutParams(((TextView) findViewById(R.id.textView6)).getLayoutParams());
                        JSONObject data = null;
                        try {
                            data = new JSONObject(snap.getValue().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.exit(-1);
                        }
                        try {
                            dynamicTextView.setText(data.getString("name") + ": " + data.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.exit(-1);
                        }
                        ((LinearLayout) findViewById(R.id.chatData)).addView(dynamicTextView);
                        try {
                            System.out.println(data.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    add = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(null,"Failed to read value.", error.toException());
            }
        });
    }

    public void send(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/rooms/" + TempStorage.get("ROOM"));
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", (String)TempStorage.get("name"));
        data.put("message", (String)((EditText)findViewById(R.id.editTextTextPersonName2)).getText().toString());
        myRef.push().updateChildren(data);
    }
}