package com.example.progettobiancotodaro.script;

import android.util.Log;

import com.example.progettobiancotodaro.DateOnDB;
import com.example.progettobiancotodaro.RatingAVGOnDB;
import com.example.progettobiancotodaro.RatingBigOnDB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class BigToAvg {
    static final long intervalHR = 5;
    public static void update(long currentDateTime) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("lastUpdate");
        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DateOnDB lastUpdate = new DateOnDB(0);
                boolean go=false;
                try {
                    lastUpdate  = dataSnapshot.getValue(DateOnDB.class);
                }catch(Exception e){
                    go=true;
                }
                //AGGIUNGO 2hr per -> considero il fusorario di ROME
                DateOnDB currentDate = new DateOnDB(currentDateTime);
                long diff = currentDate.getTimestamp()-lastUpdate.getTimestamp();
                Log.d("LAST UPDATE:", "lasUpdate is: " + lastUpdate);
                Log.d("CURRENT DATE:", "Current date is: " + currentDate);
                Log.d("DIFF:", String.valueOf(diff));
                long sec = diff / 1000;
                long min = sec / 60;
                long hr = min / 60;
                Log.d("MIN:", "" + min);
                Log.d("HR:", "" + hr);
                if (hr > intervalHR || go) {
                    translateDB();
                    //SET NUOVA DATA
                    Log.d("DATA on DB :", "Sto USANDO IL DB"); //ratingBig
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("lastUpdate");
                    mDatabase.setValue(currentDate);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("value", "Failed to read value.", error.toException());
            }


        });

    }

    static public void translateDB() {
        List<RatingBigOnDB> r = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("ratingBig");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    r.add(d.getValue(RatingBigOnDB.class));
                }
                Map<String, String> m = new HashMap<>();

                for (RatingBigOnDB rb : r) {
                    if (!m.containsKey(rb.getNumero())) {
                        m.put(rb.getNumero(), rb.getVoto() + ";");
                    } else {
                        String voti = m.get(rb.getNumero()) + rb.getVoto() + ";";
                        m.put(rb.getNumero(), voti);
                    }
                }
                // Log.d("MAP",m.toString());
                Map<String, RatingAVGOnDB> medie = new HashMap<>();
                for (Map.Entry<String, String> entry : m.entrySet()) {
                    //System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                    String voti = entry.getValue();
                    double avg = CalculateAvgRating(voti);
                    medie.put(entry.getKey(), new RatingAVGOnDB(avg));
                }
                // Log.d("MAPPE AVG:",medie.toString());
                DatabaseReference mref = FirebaseDatabase.getInstance().getReference("ratingAVG");

                for (Map.Entry<String, RatingAVGOnDB> entry : medie.entrySet()) {
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
    public static double CalculateAvgRating(String s) {
        StringTokenizer stringTokenizer = new StringTokenizer(s, ";");
        double avg = 0;
        double i = 0;
        while (stringTokenizer.hasMoreTokens()) {
            avg += Double.parseDouble(stringTokenizer.nextToken());
            i++;
        }

        if (i == 0) return 0;

        return avg / i;
    }
}


