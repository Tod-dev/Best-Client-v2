package it.bestclient.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static it.bestclient.android.Utils.displayRatingStars;

public class RatingActivity extends AppCompatActivity {

    public static final String VOTO = "voto";
    public static final String NUMBER = "numero";
    public static final String COMMENT = "commento";
    public static final String MEDIO = "medio";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        RatingBar ratingbar = findViewById(R.id.ratingStars);
        TextView number = findViewById(R.id.number);
        TextView comment = findViewById(R.id.comment);
        RatingBar ratingAVG = findViewById(R.id.ratingStarsAVG);

        Intent i = getIntent();

        final double ratingLocale = i.getDoubleExtra(VOTO,0);
        final String phoneNumber = i.getStringExtra(NUMBER);
        final String commento = i.getStringExtra(COMMENT);
        final double ratingMedio =  i.getDoubleExtra(MEDIO,0);


        ratingbar.setRating((float) ratingLocale);
        number.setText(phoneNumber);
        comment.setText(commento);
        ratingAVG.setRating((float) ratingMedio);
    }
}

