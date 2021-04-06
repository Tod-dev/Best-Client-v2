package com.example.progettobiancotodaro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.progettobiancotodaro.DB.DBhelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class AddRating extends AppCompatActivity {
    ListView listView;
    TextView toolbar_text;
    ImageView arrow_back;
    Toolbar toolbar;
    DBhelper myDBhelper;
    RatingOnDB curRating;
    List<RatingOnDB> allRatings = new ArrayList<>();

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rating);
        myDBhelper = new DBhelper(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar_text = findViewById(R.id.toolbar_title);
        toolbar_text.setText(R.string.add_rating);

        arrow_back = findViewById(R.id.arrow_back);
        arrow_back.setImageResource(R.drawable.ic_baseline_arrow_back_24);
        arrow_back.setOnClickListener(v -> {
            Intent intent = new Intent(AddRating.this, MainActivity.class);
            startActivity(intent);
        });

        listView = findViewById(R.id.list);

        List<Rating> ratings = null;
        try {
            ratings = getAllRatings();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> l = ratingToString(ratings);

        ArrayAdapter <String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,l);
        listView.setAdapter(arrayAdapter);
        List<Rating> finalRatings = ratings;
        listView.setOnItemClickListener((parent, view, i1, id) -> {
            //Toast.makeText(AddRating.this,l.get(i1),Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialogMessage);
            LayoutInflater inflater = this.getLayoutInflater();
            View viewDialog = inflater.inflate(R.layout.rating_stars, null);
            RatingBar ratingbar = viewDialog.findViewById(R.id.ratingStars);
            builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        float nuovoRating = ratingbar.getRating();
                        finalRatings.get(i1).setRating(nuovoRating);
                        try {
                            UpdateData(finalRatings.get(i1));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        refreshView(finalRatings,l,listView);
                        //ratings.get(i1).setRating(rating.getRating());
                        //Toast.makeText(AddRating.this,Float.toString(ratingbar.getRating()),Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();


        });
    }

    private void UpdateData(Rating r) throws ParseException {
        int ret = myDBhelper.updateRating(r);
        if(ret == -1){
            AddData(r);
        }else{
            toastMessage("Data Successfully Updated!");
        }
        updateDB(r);
    }

    private void updateDB(Rating r){
        Log.d("ratingonDB:", "Sto USANDO IL DB");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("ratings").child(r.getPhoneNumber());
        //RatingOnDB oldfirebaseRating;
        //RatingOnDB newfirebaseRating;
        RatingOnDB r2 = new RatingOnDB(r.getPhoneNumber(),""+r.getRating());
        Log.d("ratingonDB:", r2.toString());
        ratingsRef.setValue(r2);
        toastMessage("Data On Firebase");
    }

    public void refreshView(List<Rating> ratings, List<String> l, ListView listView){
        l.clear();
        for(Rating r: ratings){
            l.add("\n"+r.toString());
        }
        ArrayAdapter <String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,l);
        listView.setAdapter(arrayAdapter);
    }


    public List<Rating> getAllRatings() throws ParseException {

        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date"}, null, null, "date DESC");

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        final int limit = 50;

        int i = 0;

        List<Rating> ratings = new ArrayList<>();

        while(c.moveToNext() && i < limit){
            //Log.d("i, array:  ", ""+i + Arrays.toString(ratings.toArray()));
            String number = c.getString(colNumber);
            Date date = new Date(Long.parseLong(c.getString(colDate)));
            Rating check = new Rating(number,date);
            //Log.d("Data: ", ""+c.getString(colDate));


            boolean checkIfExist = false;
            for(Rating r: ratings){
                boolean res = r.group_by_number(check);
                //Log.d("CHECK_CONFRONTO: ", ""+res);
                if(res){
                    checkIfExist = true;
                    break;
                }
            }

            if(!checkIfExist){
                ratings.add(new Rating(number, date));
            }
            i++;
        }
        c.close();

        // GET DATA FROM DB

        getAllRatingsFromDB();

        Cursor data = myDBhelper.getData();
        List<Rating> listData = new ArrayList<>();
        while(data.moveToNext()){
            //get the value from the database in column 1
            //then add it to the ArrayList
            String cell = data.getString(1);
            String date = data.getString(2);
            float rating = data.getFloat(3);

            if(rating != -1)
                listData.add(new Rating(cell,new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).parse(date),rating));
            else
                listData.add(new Rating(cell,new SimpleDateFormat("dd/MM/yyyy",Locale.ITALY).parse(date)));

        }

        for(Rating r: ratings){
            for(Rating j: listData){
                if(r.group_by_number(j)){
                    r.setRating(j.getRating());
                }
            }
        }

        Log.d("lista db: ",Arrays.toString(listData.toArray()));

        return ratings;

    }

    public List<String> ratingToString(List<Rating> ratings){
        List<String> l = new ArrayList<>();

        for(Rating r: ratings){
            l.add("\n"+r.toString());
        }
        return l;
    }

    public void AddData(Rating r) {
        boolean insertData = myDBhelper.addData(r.getPhoneNumber(),r.getDate(),r.getRating());

        if (insertData) {
            toastMessage("Data Successfully Inserted!");
        } else {
            toastMessage("Something went wrong");
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }



    //Downoads all ratings in the db and puts them into allRatings List
    public void getAllRatingsFromDB(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("ratings");
        //allRatings.clear();

        ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    RatingOnDB r = d.getValue(RatingOnDB.class);
                    allRatings.add(r);
                    Toast.makeText(AddRating.this, "Sono in lettura"+r.toString(), Toast.LENGTH_LONG).show();
                }
                //Toast.makeText(AddRating.this, "Sono in lettura"+allRatings.get(0).toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Main", "Failed to read value.", error.toException());
            }
        });
    }

    //Adds a new rating on db
    public void AddNewRating(RatingOnDB ratingOnDB){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("ratings");

        ratingsRef.child(ratingOnDB.getPhoneNumber()).setValue(ratingOnDB);
    }


}