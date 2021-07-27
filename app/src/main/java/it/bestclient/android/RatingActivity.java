package it.bestclient.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import it.bestclient.android.RatingModel.RatingBigOnDB;
import it.bestclient.android.components.Contact;

import static it.bestclient.android.Utils.USERS;
import static it.bestclient.android.Utils.VALUTAZIONI;

public class RatingActivity extends AppCompatActivity {

    public static final String VOTO = "voto";
    public static final String NUMBER = "numero";
    public static final String COMMENT = "commento";
    public static final String MEDIO = "medio";
    public static final String PUBBLICA = "pubblica";
    public static final String FEEDBACK = "feedback";
    public static final String NOME = "nome";

    Context context;

    Double ratingLocale;
    String phoneNumber;
    String commento;
    Double ratingMedio;
    boolean pubblica;
    String commentList;

    RatingBar ratingbar;
    Button conferma;
    EditText comment;
    CheckBox pubblico;
    TextView feedbacks;

    Double RATINGLOCALEiniziale;
    String COMMENTOiniziale;

    boolean isKeyboardShowing = false;


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        ratingbar = findViewById(R.id.ratingStars);
        conferma = findViewById(R.id.conferma);
        TextView number = findViewById(R.id.number);
        comment = findViewById(R.id.comment);
        RatingBar ratingAVG = findViewById(R.id.ratingStarsAVG);
        pubblico = findViewById(R.id.checkBox);
        //feedbacks = findViewById(R.id.elencoFeedbacks);
        context = this;
        Intent i = getIntent();


        LinearLayout l = findViewById(R.id.ratingActivity);

        /*is keyboard opened ?*/
        l.getViewTreeObserver().addOnGlobalLayoutListener( () -> {
            Rect r = new Rect();
            l.getWindowVisibleDisplayFrame(r);
            int screenHeight = l.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            isKeyboardShowing = keypadHeight > screenHeight * 0.15;
            if(isKeyboardShowing){
                /*click outside keyboard close it */
                l.setOnClickListener(v -> {
                    v.setBackground(null);
                    Log.d("TASTIERA",isKeyboardShowing+"");
                        View view = RatingActivity.this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                });
            }else{
                l.setClickable(false);
            }
        });


        ratingLocale = i.getDoubleExtra(VOTO,0);
        phoneNumber = i.getStringExtra(NUMBER);
        commento = i.getStringExtra(COMMENT);
        ratingMedio =  i.getDoubleExtra(MEDIO,0);
        pubblica = i.getBooleanExtra(PUBBLICA, false);
        commentList = i.getStringExtra(FEEDBACK);

        RATINGLOCALEiniziale = ratingLocale;
        COMMENTOiniziale = commento;


        String actualNumber = phoneNumber;

        if(!i.getStringExtra(NOME).isEmpty()){
            actualNumber = i.getStringExtra(NOME);
        }

        ratingbar.setRating(ratingLocale.floatValue());
        pubblico.setChecked(pubblica);
        number.setText(actualNumber);
        comment.setText(commento);
        ratingAVG.setRating(ratingMedio.floatValue());

        LinearLayout layout = findViewById(R.id.listLayout);
        if(!commentList.equals("")){
            List<String> commentString = splitComments(commentList);
            // StringBuilder stringBuilder = new StringBuilder();

            for(String comment: commentString){
                //stringBuilder.append("➡ ").append(comment).append("\n");
                TextView newTextView = new TextView(this);
                newTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_campaign_24, 0, 0, 0);
                String text = " "+comment;
                newTextView.setText(text);
                newTextView.setTextColor(getResources().getColor(R.color.grey));
                newTextView.setGravity(Gravity.CENTER);
                layout.addView(newTextView);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.setMargins(50,5,50,5);
                newTextView.setLayoutParams(params);
            }
        }else{
            TextView newTextView = new TextView(this);
            newTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_campaign_24, 0, 0, 0);
            newTextView.setText(R.string.noFeedback);
            newTextView.setTextColor(getResources().getColor(R.color.grey));
            newTextView.setGravity(Gravity.CENTER);
            layout.addView(newTextView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.setMargins(50,5,50,5);
            newTextView.setLayoutParams(params);
        }

        conferma.setOnClickListener(v -> {
            Log.d("CHECKBOX_VALUE",pubblico.isChecked()+"");
            long dateRatingBig = new Date().getTime();
            String commentoRatingBIG = comment.getText().toString();
            double votoRatingBig = ratingbar.getRating();

            String uid = this.getSharedPreferences("UserPreferences", MODE_PRIVATE).getString("uid", "");

            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(USERS).child(uid).child(VALUTAZIONI).child(phoneNumber);

            Toast.makeText(context, "Valutazione aggiornata!", Toast.LENGTH_SHORT).show();

            // Log.d("BACK--",myRef.toString());

            RatingBigOnDB newValues = new RatingBigOnDB(dateRatingBig,commentoRatingBIG,votoRatingBig);
            newValues.setPubblica(pubblico.isChecked());

            myRef.setValue(newValues);
            Intent intent = new Intent(RatingActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.to_left_in, R.anim.to_right_out);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.to_left_in, R.anim.to_right_out);
    }

    public List<String> splitComments(String comments){
        List<String> commentString = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(comments, "#");
        while(stringTokenizer.hasMoreTokens()){
            commentString.add(stringTokenizer.nextToken());
        }

        return commentString;
    }
}

