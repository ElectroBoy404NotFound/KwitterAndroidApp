package me.electronicsboy.kwitterandroidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class KwitterRoom extends AppCompatActivity {

    private ArrayList<String> rooms = new ArrayList<String>();
    private AppCompatActivity inst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inst = this;
        setContentView(R.layout.activity_kwitter_room);
        rooms.add("Loading...");
        ListView listview = (ListView) findViewById(R.id.rooms);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/rooms/");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rooms.removeIf((e) -> true);
                for(DataSnapshot snap : dataSnapshot.getChildren())  rooms.add(snap.getKey());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(inst, android.R.layout.simple_list_item_1, rooms);
                listview.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                listview.setOnItemClickListener((AdapterView<?> parentView, View childView, int position, long id) -> { TempStorage.addOrSet("ROOM", rooms.get(position)); startActivity(new Intent(inst, KwitterChat.class)); });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(null,"Failed to read value.", error.toException());
            }
        });
    }

    public void onLogout(View view) {
        TempStorage.clear();
        deleteFile("login_data.dat");
        startActivity(new Intent(this, MainActivity.class));
    }
}