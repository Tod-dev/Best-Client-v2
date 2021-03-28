package com.example.progettobiancotodaro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddRating extends AppCompatActivity {
    ListView listView;
    TextView tv;
    TextView toolbar_text;
    ImageView arrow_back;
    Toolbar toolbar;

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

        //tv = findViewById(R.id.informazioni);


        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date"}, null, null, "date DESC");

        StringBuilder stringBuffer = new StringBuilder();

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        int offset = 0;
        final int limit = 50;

        int i = 0;
        while(i < offset && c.moveToNext()){
            i++;
        }

        SimpleDateFormat format = new SimpleDateFormat("dd/M/yyyy HH:mm", Locale.ITALIAN);

        while(c.moveToNext() && i < limit){
            String number = c.getString(colNumber);
            Date date = new Date(Long.parseLong(c.getString(colDate)));
            String stringDate = format.format(date);
            String finalString = "\nNumber: "+number+" Date: "+stringDate;
            //stringBuffer.append(finalString);
            l.add(finalString);
            i++;
        }
        c.close();



        //tv.setText(stringBuffer);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,l);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view, i1, id) -> Toast.makeText(AddRating.this,l.get(i1),Toast.LENGTH_SHORT).show());
    }
}