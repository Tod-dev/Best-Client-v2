package it.bestclient.android;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import it.bestclient.android.RatingModel.RatingBigOnDB;

import static it.bestclient.android.HomeActivity.showRatings;
import static it.bestclient.android.Utils.USERS;
import static it.bestclient.android.Utils.VALUTAZIONI;

public class RatingActivity extends AppCompatActivity {

    public static final String VOTO = "voto";
    public static final String NUMBER = "numero";
    public static final String COMMENT = "commento";
    public static final String MEDIO = "medio";

    Context context;

    Double ratingLocale;
    String phoneNumber;
    String commento;
    Double ratingMedio;

    RatingBar ratingbar;
    Button conferma;
    EditText comment;

    Double RATINGLOCALEiniziale;
    String COMMENTOiniziale;

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        ratingbar = findViewById(R.id.ratingStars);
        conferma = findViewById(R.id.conferma);
        TextView number = findViewById(R.id.number);
        comment = findViewById(R.id.comment);
        RatingBar ratingAVG = findViewById(R.id.ratingStarsAVG);
        context = this;

        Intent i = getIntent();

        /*click outside keyboard close it*/
        ConstraintLayout l = findViewById(R.id.ratingActivity);
        l.setOnClickListener(v -> {
            View view = RatingActivity.this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

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

        conferma.setOnClickListener(v -> {
            String dateRatingBig = new Date().toString();
            String commentoRatingBIG = comment.getText().toString();
            double votoRatingBig = ratingbar.getRating();

            String uid = this.getSharedPreferences("UserPreferences", MODE_PRIVATE).getString("uid", "");

            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(USERS).child(uid).child(VALUTAZIONI).child(phoneNumber);

            Toast.makeText(context, "Valutazione aggiornata!", Toast.LENGTH_SHORT).show();

            // Log.d("BACK--",myRef.toString());

            RatingBigOnDB newValues = new RatingBigOnDB(dateRatingBig,commentoRatingBIG,votoRatingBig);

            myRef.setValue(newValues);
            showRatings(this);
        });
    }
}

