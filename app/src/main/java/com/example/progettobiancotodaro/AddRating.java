package com.example.progettobiancotodaro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class AddRating extends AppCompatActivity {
    TextView tv;
    Button homepagebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rating);

        homepagebtn = findViewById(R.id.button);
        homepagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddRating.this, MainActivity.class);
                startActivity(intent);
            }
        });

        tv = (TextView) findViewById(R.id.informazioni);


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
        tv.setText(stringBuffer);
    }
}