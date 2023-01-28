package me.electronicsboy.kwitterandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private AppCompatActivity inst;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inst = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try
        {
            FileInputStream fin = openFileInput("login_data.dat");
            int a;
            StringBuilder temp = new StringBuilder();
            while ((a = fin.read()) != -1)
                temp.append((char)a);
            fin.close();
            String finalOut = temp.toString();
            ((EditText)findViewById(R.id.username)).setText(finalOut.split(",")[0]);
            ((EditText)findViewById(R.id.password)).setText(finalOut.split(",")[1]);
            testLogin();
        }catch(FileNotFoundException e) {
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void onLogin(View view) {
        String username = ((EditText)findViewById(R.id.username)).getText().toString();
        String password = ((EditText)findViewById(R.id.password)).getText().toString();
        if(username.equals("")) {
            ((EditText)findViewById(R.id.username)).setText("");
            Toast.makeText(this, getString(R.string.invalid_username), Toast.LENGTH_SHORT).show();
        } else if(password.equals("")) {
            ((EditText)findViewById(R.id.password)).setText("");
            Toast.makeText(this, getString(R.string.invalid_password), Toast.LENGTH_SHORT).show();
        } else testLogin();
    }
    public void onRegister(View view) {}
    private void openRooms() {
        TempStorage.addOrSet("name", ((EditText)findViewById(R.id.username)).getText().toString());
        startActivity(new Intent(this, KwitterRoom.class));
    }
    private void testLogin() {
        Toast.makeText(this, "Logging in..", Toast.LENGTH_LONG).show();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        byte[] hash;
        hash = digest.digest(((EditText)findViewById(R.id.password)).getText().toString().getBytes(StandardCharsets.UTF_8));
        System.out.println(((EditText)findViewById(R.id.password)).getText().toString());
        System.out.println(bytesToHex(hash));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users/" + ((EditText)findViewById(R.id.username)).getText().toString());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String realPass = bytesToHex(hash);
                String value = dataSnapshot.getValue(String.class);
                System.out.println(value);

                if(realPass.equals(value)) { try
                {
                    FileOutputStream fos = openFileOutput("login_data.dat", Context.MODE_PRIVATE);
                    String data = ((EditText)findViewById(R.id.username)).getText().toString() + "," + ((EditText)findViewById(R.id.password)).getText().toString();
                    fos.write(data.getBytes());
                    fos.flush();
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                } openRooms(); }
                myRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(inst, "ERROR!\n" + error.toException().getMessage(), Toast.LENGTH_LONG);
                Log.w(null,"Failed to read value.", error.toException());
            }
        });
    }
}