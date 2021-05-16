package com.example.progettobiancotodaro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.progettobiancotodaro.DB.DBhelper;
import com.example.progettobiancotodaro.ui.login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AddRating extends AppCompatActivity {
    ListView listView;
    DBhelper myDBhelper;
    String[] phoneNumbers;
    String[] dates;
    String[] ratingString;
    //List<RatingAVGOnDB> allRatings = new ArrayList<>();
    SharedPreferences sp;
    String uid;

    public void setUid(String uid) {
        this.uid = uid;
    }

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rating);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.add_rating);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        myDBhelper = new DBhelper(this);
        //myDBhelper.addColumn();

        listView = findViewById(R.id.list);

        //SETTO L'UID
        sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String uid = sp.getString("uid", "");
        setUid(uid);
        //toastMessage(uid);

        showRatings();
    }

    public void showRatings(){
        /* PRENDO TUTTI I RATING */
        List<Rating> ratings = null;
        try {
            ratings = getAllRatings();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (ratings != null) {
            ratingToString(ratings);
        }

        /* INSERISCO TUTTI I RATING NELLA LISTVIEW */
        MyAdapter arrayAdapter = new MyAdapter(this, phoneNumbers, dates, ratingString);
        listView.setAdapter(arrayAdapter);
        List<Rating> finalRatings = ratings;
        listView.setOnItemClickListener((parent, view, i1, id) -> {
            Rating k = finalRatings.get(i1);

            /* SE HO GIA' INSERITO QUEL RATING CHIEDO SE LO VUOLE ELIMINARE DALLA LISTA */
            if( k.getRating() != -1) {
                showDialog(1,finalRatings,i1); //Dialog cancella un elemento
            }else{
                showDialog(2,finalRatings,i1); //Dialog dai un voto / cancella un elemento
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        menu.findItem(R.id.refresh).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            showRatings();
            return true;
        }
        return false;
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
                TextInputLayout comment = viewDialog.findViewById(R.id.comment);
                TextInputEditText commentText = viewDialog.findViewById(R.id.commentText);
                RatingBar ratingbar = viewDialog.findViewById(R.id.ratingStars);
                ImageView deleteButton = viewDialog.findViewById(R.id.delete);
                ImageView commentButton = viewDialog.findViewById(R.id.commentLogo);

                if(!finalRatings.get(index).getComment().equals("")){
                    commentText.setText(finalRatings.get(index).getComment());
                }

                builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        float nuovoRating =  ratingbar.getRating();

                        if(!Objects.requireNonNull(comment.getEditText()).getText().toString().equals("")){
                            finalRatings.get(index).setComment(comment.getEditText().getText().toString());
                        }
                        if(nuovoRating != 0){
                            //se ho inserito un rating modifico il db firebase
                            finalRatings.get(index).setRating(nuovoRating);
                            try {
                                UpdateData(finalRatings.get(index), true);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            if(!comment.getEditText().getText().toString().equals("")){
                                try {
                                    UpdateData(finalRatings.get(index), false);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
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
            commentButton.setOnClickListener(v -> {
                comment.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.INVISIBLE);
            });

        }
    }

    private void UpdateData(Rating r,boolean db) throws ParseException {
        /* AGGIORNA I DATI SUL DB SQLITE E SU FIREBASE */
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        //System.out.println(formatter.format(date));
        RatingBigOnDB remoteRating = new RatingBigOnDB(uid,formatter.format(date),r.getRating(),r.getPhoneNumber(),r.getComment());

        if(db)
            updateDB(remoteRating);

        if(r.getRating() > 0){
            r.setRating(-2);
        }

        int ret = myDBhelper.updateRating(r);
        if(ret == -1){
            AddData(r);
        }else{
           // toastMessage("Data Successfully Updated!");
            Log.d("DATA IN LOCALE", "UpdateData: ");
        }
    }

    private void updateDB(RatingBigOnDB r){
        Log.d("ratingonDB:", "Sto USANDO IL DB"); //ratingBig
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("ratingBig");
        mDatabase.push().setValue(r);
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String notificationPreference = preferences.getString("Last Calls", "None");

        List<Rating> ratings = new ArrayList<>();

        Date curDate = Calendar.getInstance().getTime();
        //LETTURA REGISTRO CHIAMATE
        while(c.moveToNext()){
            boolean skip = false;
            //Log.d("i, array:  ", ""+i + Arrays.toString(ratings.toArray()));
            String number = c.getString(colNumber);
            Date date = new Date(Long.parseLong(c.getString(colDate)));
            Rating check = new Rating(number,date);
            //Log.d("Data: ", ""+c.getString(colDate));

            long diffInMillies = Math.abs(date.getTime() - curDate.getTime());
            long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            //Filtro in base alle impostazioni
            switch (notificationPreference) {
                case "last24":
                    if(diff > 24) skip = true;
                    break;
                case "last48":
                    if(diff > 48) skip = true;
                    break;
                default:
                    break;
            }

            if(skip) continue;

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
        }
        c.close();

        //HO TUTTO IL REGISTRO IN ratings RAGGRUPPATO PER DATA

        //PRENDO I DATI DA SQL lite PER VEDERE QUALI RATING HO GIA' INSERITO E QUALI NO
        Cursor data = myDBhelper.getData();
        List<Rating> listData = new ArrayList<>();
        while(data.moveToNext()){
            String cell = data.getString(1);
            String date = data.getString(2);
            float rating = data.getFloat(3);
            String comment = data.getString(4);
            if(rating != -1 || !comment.equals(""))
                listData.add(new Rating(cell,new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).parse(date), rating, comment));
        }

        for(Rating r: ratings){
            for(Rating j: listData){
                if(r.group_by(j)){
                    //aggiorno il rating solo se sul db ne ho già inserito uno lo stesso giorno
                    if(j.getRating() != -1){
                        r.setRating(j.getRating());
                    }
                    //Scrivo l'ultimo commento inserito
                    if(!j.getComment().equals("")){
                        r.setComment(j.getComment());
                    }
                }
            }
        }

        //filtro solo != -2 -> Iterator
        filtroNonMeno2(ratings);


       // Log.d("lista db: ",Arrays.toString(listData.toArray()));

        //SORTING (METTO PRIMA QUELLI DA INSERIRE E POI QUELLI GIA' INSERITI)
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
        for(Rating ignored : ratings){
            phoneNumbers[i] = ratings.get(i).getPhoneNumber();
            dates[i] = ratings.get(i).getDate();
            ratingString[i] = String.valueOf(ratings.get(i).getRating());
            i++;
        }
    }

    public void AddData(Rating r) {
        boolean insertData = myDBhelper.addData(r.getPhoneNumber(),r.getDate(),r.getRating(), r.getComment());

        if (insertData) {
            toastMessage("Data Successfully Inserted!");
        } else {
            toastMessage("Something went wrong");
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }


    /*
    //Downoads all ratings in the db and puts them into allRatings List
    public void getAllRatingsFromDB(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("ratings");
        //allRatings.clear();

        ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    RatingAVGOnDB r = d.getValue(RatingAVGOnDB.class);
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
    */
    /*
    //Adds a new rating on db
    public void AddNewRating(RatingAVGOnDB ratingAVGOnDB){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("ratings");

        ratingsRef.child(ratingAVGOnDB.getPhoneNumber()).setValue(ratingAVGOnDB);
    }
    */
    /* CLASSE PER LA VISIONE PERSONALIZZATA DELLA LIST VIEW */
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