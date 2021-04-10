package com.example.progettobiancotodaro;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    Button ratingButton;
    Button settingsbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(R.string.app_name);
            actionBar.setIcon(R.drawable.icona_logo);
        }


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE}, 1);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_NUMBERS},1);
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG},1);
        }

        ratingButton = findViewById(R.id.AddRatingButton);
        ratingButton.setOnClickListener(v -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ratingsRef = database.getReference("ratings");

            /*RatingOnDB r = new RatingOnDB("3349331521","4;5;3.5;2");
            ratingsRef.child("3349331521").setValue(r);*/

            Intent intent = new Intent(MainActivity.this, AddRating.class);
            startActivity(intent);
        });

        settingsbutton = findViewById(R.id.settingsButton);
        settingsbutton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }



}
/*tv = (TextView) findViewById(R.id.informazioni);


        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date"}, null, null, "date DESC");

        StringBuffer stringBuffer = new StringBuffer();

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        int offset = 0;
        final int limit = 10;

        int i = 0;
        while(i < offset && c.moveToNext()){
            i++;
        }

        while(c.moveToNext() && i < limit){
            String number = c.getString(colNumber);
            Date date = new Date(Long.valueOf(c.getString(colDate)));
            stringBuffer.append("\nNumber: "+number);
            stringBuffer.append("\nDate: "+date);
            i++;
        }
        c.close();
        tv.setText(stringBuffer);*/



/* // Read from the database
        ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                    /*String value = dataSnapshot.getValue(String.class);
                    Log.d("Main", "Value is: " + value);
                oldfirebaseRating = dataSnapshot.getValue(RatingOnDB.class);
        Log.w("OLD FIREBASE RATING", "");
                    /*for(DataSnapshot d : dataSnapshot.getChildren()){
                        r = (RatingOnDB) d.getValue(RatingOnDB.class);
                    }

                    Toast t = Toast.makeText(MainActivity.this, r.getPhoneNumber()+" "+r.getRatings(), Toast.LENGTH_LONG);
                    t.show();
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Main", "Failed to read value.", error.toException());
            }
        });
*/