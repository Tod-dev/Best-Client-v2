package com.example.progettobiancotodaro.script;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.progettobiancotodaro.RatingAVGOnDB;
import com.example.progettobiancotodaro.RatingBigOnDB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class BigToAvg {
    static final long intervalMin = 1;
    static String LastUpdate;

    public static void setLastUpdate(String lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public static void update(){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("lastUpdate");
        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                setLastUpdate(value);
                try { // SETTARE IL TIMEZONE
                    Date currentDate = new Date();
                    Date lastUpdate = formatter.parse(LastUpdate);
                    long diff = currentDate.getTime()-lastUpdate.getTime();
                    diff = Math.abs(diff);
                    Log.d("LAST UPDATE:", "lasUpdate is: " + formatter.format(lastUpdate));
                    Log.d("CURRENT DATE:", "Current date is: " + formatter.format(currentDate));
                    Log.d("DIFF:", String.valueOf(diff));
                    long sec = diff/1000;
                    long min = sec/60;
                    long hr = min/60;
                    Log.d("MIN:", ""+min);
                    Log.d("HR:", ""+hr);
                    if(min > intervalMin){
                        translateDB();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("value", "Failed to read value.", error.toException());
            }



        });

    }

    static public void translateDB(){
        List <RatingBigOnDB> r = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("ratingBig");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for(DataSnapshot d :  dataSnapshot.getChildren()){
                    r.add(d.getValue(RatingBigOnDB.class));
                }
                Map<String,String> m = new HashMap<>();

                for(RatingBigOnDB rb : r){
                    if(!m.containsKey(rb.getNumero())){
                        m.put(rb.getNumero(),rb.getVoto()+";");
                    }else{
                        String voti = m.get(rb.getNumero())+rb.getVoto()+";";
                        m.put(rb.getNumero(),voti);
                    }
                }
               // Log.d("MAP",m.toString());
                Map <String, RatingAVGOnDB> medie = new HashMap<>();
                for(Map.Entry<String, String> entry : m.entrySet()){
                    //System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                    String voti=entry.getValue();
                    double avg = CalculateAvgRating(voti);
                    medie.put(entry.getKey(),new RatingAVGOnDB(avg));
                }
               // Log.d("MAPPE AVG:",medie.toString());
                DatabaseReference mref  = FirebaseDatabase.getInstance().getReference("ratingAVG");

                for(Map.Entry<String, RatingAVGOnDB> entry : medie.entrySet()){
                    //System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                    mref.child(entry.getKey()).setValue(entry.getValue().getRating());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("FAILED", "Failed to read value.", error.toException());
            }
        });
    }
    //Calculates average rating of a RatingOnDB object
    public static double CalculateAvgRating(String s){
        StringTokenizer stringTokenizer = new StringTokenizer(s, ";");
        double avg = 0;
        double i = 0;
        while(stringTokenizer.hasMoreTokens()){
            avg += Double.parseDouble(stringTokenizer.nextToken());
            i++;
        }

        if(i == 0) return 0;

        return avg / i;
    }
}


