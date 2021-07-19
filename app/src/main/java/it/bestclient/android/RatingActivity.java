package it.bestclient.android;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RatingActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        RatingBar ratingbar = findViewById(R.id.ratingStars);
        TextView number = findViewById(R.id.number);
        TextView comment = findViewById(R.id.comment);
        TextView ratingAVG = findViewById(R.id.ratingAVG);

        final float ratingLocale = 3.5f;
        final String phoneNumber = "3312511781";
        final String commento = "commento di prova";
        final String ratingMedio = "⭐⭐⭐⭐";

        ratingbar.setRating(ratingLocale);
        number.setText(phoneNumber);
        comment.setText(commento);
        ratingAVG.setText(ratingMedio);
    }
}

