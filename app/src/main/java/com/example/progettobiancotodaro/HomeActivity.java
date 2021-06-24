package com.example.progettobiancotodaro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.progettobiancotodaro.DB.DBhelper;
import com.example.progettobiancotodaro.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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


@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeActivity extends AppCompatActivity {

    // GoogleSignInClient mGoogleSignInClient;
    Button ratingButton;
    String[] Permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_CALL_LOG};
    SharedPreferences sp;
    ListView listView;
    DBhelper myDBhelper;
    String[] phoneNumbers;
    String[] dates;
    String[] commentString;
    //List<RatingAVGOnDB> allRatings = new ArrayList<>();
    String uid;
    final int MAX_ITEMS = 100;
    //final String uri = "http://worldtimeapi.org/api/timezone/Europe/Rome";

    public void setUid(String uid) {
        this.uid = uid;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bn = findViewById(R.id.bottomMenu);
        bn.setSelectedItemId(R.id.homeBtn);
        bn.setOnNavigationItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.homeBtn:{
                    break;
                }
                case R.id.settingsBtn:{
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;
                }
                case R.id.profileBtn:{
                    Intent intent = new Intent(HomeActivity.this, Profile.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;
                }
                default: break;
            }

            return true;
        });

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.home);

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Permissions, 1);
        }

        /*ratingButton = findViewById(R.id.AddRatingButton);
        ratingButton.setOnClickListener(v -> {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)) {
                //se ha i permessi avvia add rating
                Intent intent = new Intent(HomeActivity.this, AddRating.class);
                startActivity(intent);
            } else
                Toast.makeText(this, "This app couldn't read phone numbers and call log, please allow in settings", Toast.LENGTH_LONG).show();

        });*/

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)) {
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
        /*
        else
            Toast.makeText(this, "This app couldn't read phone numbers and call log, please allow in settings", Toast.LENGTH_LONG).show();
         */
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
        HomeActivity.MyAdapter arrayAdapter = new HomeActivity.MyAdapter(this, phoneNumbers, dates, commentString);
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
        HomeActivity.MyAdapter arrayAdapter = new HomeActivity.MyAdapter(this, phoneNumbers, dates, commentString);
        listView.setAdapter(arrayAdapter);
    }

    private void filtroNonMeno2(List<Rating> r){
        for(Iterator<Rating> k = r.iterator(); k.hasNext();){
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
        int count = 0;
        while(c.moveToNext()){
            count++;
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
                    if(count > MAX_ITEMS) skip = true;
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

        return notRatedFirst;

    }

    public void ratingToString(List<Rating> ratings){
        phoneNumbers = new String[ratings.size()];
        dates = new String[ratings.size()];
        commentString = new String[ratings.size()];

        int i = 0;
        for(Rating ignored : ratings){
            phoneNumbers[i] = ratings.get(i).getPhoneNumber();
            dates[i] = ratings.get(i).getDate();
            commentString[i] = ratings.get(i).getComment(); //String.valueOf(ratings.get(i).getRating());
            i++;
        }
    }

    public void AddData(Rating r) {
        boolean insertData = myDBhelper.addData(r.getPhoneNumber(),r.getDate(),r.getRating(), r.getComment());

        if (insertData) {
            toastMessage("Valutazione inserita correttamente!");
        } else {
            toastMessage("Qualcosa è andato storto :(");
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
    class MyAdapter extends ArrayAdapter<String> {
        Context context;
        String[] rPhoneNumber;
        String[] rDate;
        String[] rComment;

        MyAdapter(Context context, String[] phoneNumber, String[] date, String[] comment){
            super(context,R.layout.rows,R.id.phoneNumber, phoneNumber);
            this.context = context;
            this.rPhoneNumber = phoneNumber;
            this.rDate = date;
            this.rComment = comment;
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
            dateView.setText(rDate[position]);
            if(rComment[position].equals("")){
                ratingView.setText("No comment");
            }
            else ratingView.setText(rComment[position]);

            return row;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String email = sp.getString("email", "");
        String password = sp.getString("password", "");

        if (email.equals("") || password.equals("")) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        }

    }

}
    /*
    private void requestData() {
        Log.d("REQUEST DATA:","SONO IN REQUEST DATA");
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(uri);

        Downloader downloader = new Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }
*/
    /*
    private static class Downloader extends AsyncTask<RequestPackage, String, String> {
        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        //The String that is returned in the doInBackground() method is sent to the
        // onPostExecute() method below. The String should contain JSON data.
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.d("DOWNLOADER:","SONO IN DOWNLOADER : "+result);
                //We need to convert the string in result to a JSONObject
                if(result == null) return;
                JSONObject jsonObject = new JSONObject(result);
                Log.d("JSON:",jsonObject.toString());
                //The “ask” value below is a field in the JSON Object that was
                //retrieved from the BitcoinAverage API. It contains the current
                //bitcoin price
                long unixTime = jsonObject.getLong("unixtime");
                unixTime *= 1000; //timestamp in ms
                Log.d("Data:",""+unixTime);
                BigToAvg.update(unixTime);

                //Now we can use the value in the mPriceTextView
                //mPriceTextView.setText(price);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}*/
