package com.example.progettobiancotodaro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AddRating extends AppCompatActivity {
    ListView listView;
    TextView toolbar_text;
    ImageView arrow_back;
    Toolbar toolbar;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rating);

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
        List<String> l = new ArrayList<>();

        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date"}, null, null, "date DESC");


        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);


        //int offset = 0;
        final int limit = 50;
        int days_group_by = 1;

        int i = 0;
        /*
        while(i < offset && c.moveToNext()){
            i++;
        }
        Log.d("i: ", ""+i);
        */

        List<Rating> ratings = new ArrayList<>();

        while(c.moveToNext() && i < limit){
            //Log.d("i, array:  ", ""+i + Arrays.toString(ratings.toArray()));
            String number = c.getString(colNumber);
            Date date = new Date(Long.parseLong(c.getString(colDate)));
            Rating check = new Rating(number,date);
            //Log.d("Data: ", ""+c.getString(colDate));


            boolean checkIfExist = false;
            for(Rating r: ratings){
                boolean res = r.equals(check,days_group_by);
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

        for(Rating r: ratings){
            l.add("\n"+r.toString());
        }

        ArrayAdapter <String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,l);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, i1, id) -> {
            //Toast.makeText(AddRating.this,l.get(i1),Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialogMessage);
            LayoutInflater inflater = this.getLayoutInflater();
            View viewDialog = inflater.inflate(R.layout.rating_stars, null);
            RatingBar ratingbar = viewDialog.findViewById(R.id.ratingStars);
            builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        ratings.get(i1).setRating(ratingbar.getRating());
                        refreshView(ratings,l,listView);
                        //ratings.get(i1).setRating(rating.getRating());
                        //Toast.makeText(AddRating.this,Float.toString(ratingbar.getRating()),Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();


        });
    }

    public void refreshView(List<Rating> ratings, List<String> l, ListView listView){
        l.clear();
        for(Rating r: ratings){
            l.add("\n"+r.toString());
        }
        ArrayAdapter <String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,l);
        listView.setAdapter(arrayAdapter);
    }
}