package com.example.progettobiancotodaro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.progettobiancotodaro.DB.DBhelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddRating extends AppCompatActivity {
    ListView listView;
    TextView toolbar_text;
    Toolbar toolbar;
    DBhelper myDBhelper;
    RatingOnDB curRating;
    String[] phoneNumbers;
    String[] dates;
    String[] ratingString;
    List<RatingOnDB> allRatings = new ArrayList<>();

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rating);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.add_rating);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.drawable.ic_baseline_arrow_back_24);
        }


        myDBhelper = new DBhelper(this);
        /*toolbar = findViewById(R.id.toolbar);
        toolbar_text = findViewById(R.id.toolbar_title);
        toolbar_text.setText(R.string.add_rating);

        arrow_back = findViewById(R.id.arrow_back);
        arrow_back.setImageResource(R.drawable.ic_baseline_arrow_back_24);
        arrow_back.setOnClickListener(v -> {
            Intent intent = new Intent(AddRating.this, MainActivity.class);
            startActivity(intent);
        });*/

        listView = findViewById(R.id.list);

        List<Rating> ratings = null;
        try {
            ratings = getAllRatings();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (ratings != null) {
            ratingToString(ratings);
        }

        MyAdapter arrayAdapter = new MyAdapter(this, phoneNumbers, dates, ratingString);
        listView.setAdapter(arrayAdapter);
        List<Rating> finalRatings = ratings;
        listView.setOnItemClickListener((parent, view, i1, id) -> {
            Rating k = finalRatings.get(i1);
            if( k.getRating() != -1) {
                showDialog(1,finalRatings,i1); //Dialog cancella un elemento
            }else{
                showDialog(2,finalRatings,i1); //Dialog dai un voto / cancella un elemento
            }
        });
    }

    private void showDialog(int type, List <Rating> finalRatings, int index){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        if(type == 1 ) { //Dialog cancella un elemento
            builder.setTitle(R.string.dialogMessage2);
            View viewDialog = inflater.inflate(R.layout.delete_rating, null);
            builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        float nuovoRating = -2;
                        finalRatings.get(index).setRating(nuovoRating);
                        try {
                            UpdateData(finalRatings.get(index), false);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        refreshView(finalRatings, listView);
                        //ratings.get(i1).setRating(rating.getRating());
                        //Toast.makeText(AddRating.this,Float.toString(ratingbar.getRating()),Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if (type == 2) { //Dialog dai un voto / cancella un elemento
                builder.setTitle(R.string.dialogMessage);
                View viewDialog = inflater.inflate(R.layout.rating_stars, null);
                RatingBar ratingbar = viewDialog.findViewById(R.id.ratingStars);
                ImageView deleteButton = viewDialog.findViewById(R.id.delete);
                builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        float nuovoRating =  ratingbar.getRating();
                        finalRatings.get(index).setRating(nuovoRating);
                        try {
                            UpdateData(finalRatings.get(index), true);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        refreshView(finalRatings, listView);
                        //ratings.get(i1).setRating(rating.getRating());
                        //Toast.makeText(AddRating.this,Float.toString(ratingbar.getRating()),Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
            deleteButton.setOnClickListener(v -> {
                dialog.dismiss();
                showDialog(1,finalRatings,index);
            });

        }
    }

    private void UpdateData(Rating r,boolean db) throws ParseException {
        int ret = myDBhelper.updateRating(r);
        if(ret == -1){
            AddData(r);
        }else{
            toastMessage("Data Successfully Updated!");
        }
        if(db)
        updateDB(r);
    }

    private void updateDB(Rating r){
        Log.d("ratingonDB:", "Sto USANDO IL DB");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("ratings").child(r.getPhoneNumber());
        //RatingOnDB oldfirebaseRating;
        //RatingOnDB newfirebaseRating;
        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    RatingOnDB ratingOnDB = new RatingOnDB(r.getPhoneNumber(), r.getRating()+";");
                    ratingsRef.setValue(ratingOnDB);
                }
                else{
                    RatingOnDB ratingOnDB = dataSnapshot.getValue(RatingOnDB.class);
                    Objects.requireNonNull(ratingOnDB).setRatings(ratingOnDB.getRatings()+""+r.getRating()+";");
                    ratingsRef.setValue(ratingOnDB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*RatingOnDB r2 = new RatingOnDB(r.getPhoneNumber(),""+r.getRating());
        Log.d("ratingonDB:", r2.toString());
        ratingsRef.setValue(r2);
        toastMessage("Data On Firebase");*/
    }

    public void refreshView(List<Rating> ratings, ListView listView){
        filtroNonMeno2(ratings);
        ratingToString(ratings);
        MyAdapter arrayAdapter = new MyAdapter(this, phoneNumbers, dates, ratingString);
        listView.setAdapter(arrayAdapter);
    }

    private void filtroNonMeno2(List<Rating> r){
        for(Iterator<Rating>k = r.iterator();k.hasNext();){
            if(k.next().getRating() == -2){
                k.remove();
            }
        }
    }


    public List<Rating> getAllRatings() throws ParseException {

        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date"}, null, null, "date DESC");

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        //final int limit = 50;

        int i = 0;

        List<Rating> ratings = new ArrayList<>();

        //LETTURA REGISTRO CHIAMATE
        while(c.moveToNext()){
            //Log.d("i, array:  ", ""+i + Arrays.toString(ratings.toArray()));
            String number = c.getString(colNumber);
            Date date = new Date(Long.parseLong(c.getString(colDate)));
            Rating check = new Rating(number,date);
            //Log.d("Data: ", ""+c.getString(colDate));


            boolean checkIfExist = false;
            for(Rating r: ratings){
                boolean res = r.group_by(check);
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

        //getAllRatingsFromDB();

        //SQL lite
        Cursor data = myDBhelper.getData();
        List<Rating> listData = new ArrayList<>();
        while(data.moveToNext()){
            String cell = data.getString(1);
            String date = data.getString(2);
            float rating = data.getFloat(3);
            if(rating != -1)
                listData.add(new Rating(cell,new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).parse(date),rating));
        }

        for(Rating r: ratings){
            for(Rating j: listData){
                if(r.group_by(j)){
                    r.setRating(j.getRating());
                }
            }
        }

        //filtro solo != -2 -> Iterator
        filtroNonMeno2(ratings);


       // Log.d("lista db: ",Arrays.toString(listData.toArray()));

        //SORTING
        List<Rating> notRatedFirst = new ArrayList<>();

        for( Rating r: ratings){
            if(r.getRating() == -1){
                notRatedFirst.add(r);
            }
        }
        for( Rating r: ratings){
            if(r.getRating() != -1){
                notRatedFirst.add(r);
            }
        }

        return notRatedFirst;

    }

    public void ratingToString(List<Rating> ratings){
        phoneNumbers = new String[ratings.size()];
        dates = new String[ratings.size()];
        ratingString = new String[ratings.size()];

        int i = 0;
        for(Rating r: ratings){
            phoneNumbers[i] = ratings.get(i).getPhoneNumber();
            dates[i] = ratings.get(i).getDate();
            ratingString[i] = String.valueOf(ratings.get(i).getRating());
            i++;
        }
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
                    //Toast.makeText(AddRating.this, "Sono in lettura"+r.toString(), Toast.LENGTH_LONG).show();
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

    class MyAdapter extends ArrayAdapter<String>{
        Context context;
        String[] rPhoneNumber;
        String[] rDate;
        String[] rRating;

        MyAdapter(Context context, String[] phoneNumber, String[] date, String[] rating){
            super(context,R.layout.rows,R.id.phoneNumber, phoneNumber);
            this.context = context;
            this.rPhoneNumber = phoneNumber;
            this.rDate = date;
            this.rRating = rating;
        }

        @SuppressLint("SetTextI18n")
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder")
            View row = layoutInflater.inflate(R.layout.rows, parent, false);
            TextView phoneNumberView = row.findViewById(R.id.phoneNumber);
            TextView dateView = row.findViewById(R.id.date);
            TextView ratingView = row.findViewById(R.id.rating);

            phoneNumberView.setText(rPhoneNumber[position]);
            dateView.setText("Last call: "+rDate[position]);
            if(rRating[position].equals("-1.0")){
                ratingView.setText("Rating: -");
            }
            else ratingView.setText("Rating: "+rRating[position]);

            return row;
        }
    }


}