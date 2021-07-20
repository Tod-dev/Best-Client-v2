package it.bestclient.android;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import it.bestclient.android.RatingModel.RatingBigOnDB;

import static android.view.View.GONE;
import static it.bestclient.android.HomeActivity.showRatings;
import static it.bestclient.android.Utils.USERS;
import static it.bestclient.android.Utils.VALUTAZIONI;
import static it.bestclient.android.Utils.displayRatingStars;
import static it.bestclient.android.Utils.toastMessage;

public class RatingActivity extends AppCompatActivity {

    public static final String VOTO = "voto";
    public static final String NUMBER = "numero";
    public static final String COMMENT = "commento";
    public static final String MEDIO = "medio";

    Double ratingLocale;
    String phoneNumber;
    String commento;
    Double ratingMedio;

    RatingBar ratingbar;
    EditText comment;

    Double RATINGLOCALEiniziale;
    String COMMENTOiniziale;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        ratingbar = findViewById(R.id.ratingStars);
        TextView number = findViewById(R.id.number);
        comment = findViewById(R.id.comment);
        RatingBar ratingAVG = findViewById(R.id.ratingStarsAVG);

        Intent i = getIntent();



        ratingLocale = i.getDoubleExtra(VOTO,0);
        phoneNumber = i.getStringExtra(NUMBER);
        commento = i.getStringExtra(COMMENT);
        ratingMedio =  i.getDoubleExtra(MEDIO,0);

        RATINGLOCALEiniziale = ratingLocale;
        COMMENTOiniziale = commento;


        ratingbar.setRating(ratingLocale.floatValue());
        number.setText(phoneNumber);
        comment.setText(commento);
        ratingAVG.setRating(ratingMedio.floatValue());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDestroy() {
        Log.d("BACK--","PRESSED");
        /*
        * AGGIORNARE SUL DB IL RATING ASSEGNATO CON ratingLOCALE, data attuale e commento
        * */
        String dateRatingBig = new Date().toString();
        String commentoRatingBIG = comment.getText().toString();
        Double votoRatingBig = (double) ratingbar.getRating();

        if(commentoRatingBIG.equals(COMMENTOiniziale) && votoRatingBig.equals(RATINGLOCALEiniziale)) {
            super.onDestroy();
            return;
        }


        String uid = this.getSharedPreferences("UserPreferences", MODE_PRIVATE).getString("uid", "");

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(USERS).child(uid).child(VALUTAZIONI).child(phoneNumber);


       // Log.d("BACK--",myRef.toString());

        RatingBigOnDB newValues = new RatingBigOnDB(dateRatingBig,commentoRatingBIG,votoRatingBig);

        myRef.setValue(newValues);
        showRatings(this);

        super.onDestroy();
    }

}

