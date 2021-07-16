package it.bestclient.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.bestclient.android.DB.DBhelper;
import it.bestclient.android.RatingModel.Rating;
import it.bestclient.android.RatingModel.RatingLocal;
import it.bestclient.android.components.Contact;
import it.bestclient.android.components.RowAdapter;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static it.bestclient.android.Utils.displayRatingStars;
import static it.bestclient.android.Utils.fetchContacts;
import static it.bestclient.android.Utils.filtroNonMeno2;


@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeActivity extends AppCompatActivity {

    // GoogleSignInClient mGoogleSignInClient;
    SharedPreferences sp;
    @SuppressLint("StaticFieldLeak")
    //static ListView listView;
    static RecyclerView recyclerView;
    static DBhelper myDBhelper;
    static String[] phoneNumbers;
    static String[] dates;
    static String[] commentString;
    static String[] ratingString;
    static String[] ratingAVGString;
    //List<RatingAVGOnDB> allRatings = new ArrayList<>();
    public static List<Contact> contacts = null;
    public static Map<String, String> contactMap = null;
    static final int MAX_ITEMS = 100;
    public static List<RatingLocal> ratings;
    //final String uri = "http://worldtimeapi.org/api/timezone/Europe/Rome";

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Menu*/
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
                    overridePendingTransition(R.anim.to_left_in, R.anim.to_right_out);
                    break;
                }
                case R.id.profileBtn:{
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.to_right_in, R.anim.to_left_out);
                    break;
                }
                default: break;
            }

            return true;
        });

        /*ActionBar set title*/
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.home);
        }

        recyclerView = findViewById(R.id.list);
        myDBhelper = new DBhelper(this);

        /*If we got Permissions -> fetch ratings*/
        if (checkPermissions()) {
            showRatings(this);
        }
    }

    public boolean checkPermissions(){
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)){

            ContentResolver contentResolver = getContentResolver();
            contacts = fetchContacts(contentResolver,this);
            for( Contact c : contacts)
                Log.d("CONTACTS: ",c.toString());

            return true;
        }

        Toast.makeText(this, "This app hasn't access to phone numbers, call log or contacts, allow in settings", Toast.LENGTH_LONG).show();
        return false;
    }

    public static void showRatings(Context context){
        /* GET ALL RATINGS */
        ratings = null;
        try {
            ratings = getAllRatings(context);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (ratings != null) {
            ratingToString(ratings, context);
            /* INSERT ALL THE RATINGS IN THE LISTVIEW */
            RowAdapter arrayAdapter = new RowAdapter(context, phoneNumbers, dates, commentString, ratingString, ratingAVGString);
            recyclerView.setAdapter(arrayAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        //List<RatingLocal> finalRatings = ratings;
        /*recyclerView.setOnItemClickListener((parent, view, i1, id) -> {
            RatingLocal k = finalRatings.get(i1);

            Utils.showDialog(context, 2, k, myDBhelper, uid);
        });*/
    }


    /*Menu creation -> add button refresh*/
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        menu.findItem(R.id.refresh).setVisible(true);
        menu.findItem(R.id.contacts).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            if(checkPermissions()){
                showRatings(this);
                return true;
            }
            return false;
        }

        if (item.getItemId() == R.id.contacts) {
            Intent intent = new Intent(HomeActivity.this, ContactsActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }





    /*EVERY TIME REFRESH BUTTON IS CLICKED -> REFRESH THE LIST OF RATINGS TO DISPLAY*/
    /*public void refreshView(List<RatingLocal> ratings, ListView listView){
        if(ratings != null && listView != null){
            filtroNonMeno2(ratings);
            ratingToString(ratings);
            HomeActivity.MyAdapter arrayAdapter = new HomeActivity.MyAdapter(this, phoneNumbers, dates, commentString, ratingString, ratingAVGString);
            listView.setAdapter(arrayAdapter);
        }
    }*/




    public static List<RatingLocal> getAllRatings(Context context) throws ParseException {

        /*GET CURSOR FOR THE CALLS LOG*/
        Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date"}, null, null, "date DESC");

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String notificationPreference = preferences.getString("Last Calls", "None");

        List<RatingLocal> ratingsRet = new ArrayList<>();

        Date curDate = Calendar.getInstance().getTime();
        /*READ CALLS LOG, LINE BY LINE*/
        int count = 0;
        while(c.moveToNext()){
            //Log.d("i, array:  ", ""+i + Arrays.toString(ratings.toArray()));
            count++;
            boolean skip = false;

            /*NEW RATING*/
            String number = c.getString(colNumber);
            Date date = new Date(Long.parseLong(c.getString(colDate)));


            if(number.isEmpty()){//salto in questo caso, non voglio un contatto vuoto
                continue;
            }

            RatingLocal check = new RatingLocal(number,date);

            long diffInMillies = Math.abs(date.getTime() - curDate.getTime());
            long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            /*FILTER in case settings say lastN*/
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

            /*IF THE RATING DOES NOT MATCH SETTINGS DATE*/
            if(skip) continue;

            /*IF THE RATING IS NOT THE FIRST ONE OF ITS TYPE(GROUP): (DAY,PHONE) -> skip*/
            boolean checkIfExist = false;
            for(RatingLocal r: ratingsRet){
                boolean res = r.group_by(check);
                //Log.d("CHECK_CONFRONTO: ", ""+res);
                if(res){
                    checkIfExist = true;
                    break;
                }
            }

            /*ADD A NEW RATING TO THE LIST*/
            if(!checkIfExist){
                ratingsRet.add(new RatingLocal(number, date));
            }
        }
        c.close();

        /*ALL THE CALL LOGS IN ratings list grouped by DATA(DAY,PHONE_NUMBER)*/

        /*GET DATA FROM SQL LITE DB (LOCAL) TO SEE IF I HAVE ALREADY INSERTED SOME RATINGS OR NOT*/
        Cursor data = myDBhelper.getData();
        List<RatingLocal> listData = new ArrayList<>();
        while(data.moveToNext()){
            String cell = data.getString(data.getColumnIndex(DBhelper.COL_CELL));
            String date = data.getString(data.getColumnIndex(DBhelper.COL_DATE));
            float rating = data.getFloat(data.getColumnIndex(DBhelper.COL_RATING));
            String comment = data.getString(data.getColumnIndex(DBhelper.COL_COMMENT));
            String firebaseKey = data.getString(data.getColumnIndex(DBhelper.COL_FIREBASE_KEY));

            //IF rating = -3 ignora perchè si riferisce ad un contatto
            if(rating == -3) continue;

            /*IF NOT RATED (-1) or comment inserted -> comment to insert*/
            if(rating != -1 || !comment.equals("")){
                if(date.equals(""))
                    listData.add(new RatingLocal(cell, null, rating, comment,firebaseKey));
                else
                    listData.add(new RatingLocal(cell, Rating.formatter.parse(date), rating, comment,firebaseKey));
            }

        }

        for(RatingLocal r: ratingsRet){
            for(RatingLocal j: listData){
                if(j.getDate().equals("")) continue;
                if(r.group_by(j)){
                    /*write in the ratings list the rating if inserted*/
                    if(j.getVoto() != -1){
                        r.setVoto(j.getVoto());
                    }
                    /*write in the ratings list the last comment inserted*/
                    if(!j.getCommento().equals("")){
                        r.setCommento(j.getCommento());
                    }
                    /*write in the ratings list the firebase key of the comment */
                    if(!j.get_firebase_key().equals("")){
                        r.set_firebase_key(j.get_firebase_key());
                    }
                }
            }
        }

        //filtro solo != -2 (RATING ELIMINATO) -> Iterator
        filtroNonMeno2(ratingsRet);


        // Log.d("lista db: ",Arrays.toString(listData.toArray()));

        //SORTING NOT INSERTED FIRST
        /*List<RatingLocal> notRatedFirst = new ArrayList<>();

        for( RatingLocal r: ratings){
            //RATING == -1 if NOT RATED YET
            if(r.getVoto() == -1){
                notRatedFirst.add(r);
            }
        }

        for( RatingLocal r: ratings){
            //RATING == -1 if NOT RATED YET
            if(r.getVoto() != -1){
                notRatedFirst.add(r);
            }
        }*/

        /*return the list to display*/
        return ratingsRet;
    }


    public static void ratingToString(List<RatingLocal> ratings, Context context){
        /*
        *save all the data in 3 parallel arrays of String data
        *in order to create the listView easily
        */

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String displayPreference = preferences.getString("Display Rating", "number");

        phoneNumbers = new String[ratings.size()];
        dates = new String[ratings.size()];
        commentString = new String[ratings.size()];
        ratingString = new String[ratings.size()];
        ratingAVGString = new String[ratings.size()];

        int i = 0;
        for(RatingLocal r : ratings){
            phoneNumbers[i] = ratings.get(i).getNumero();
            dates[i] = ratings.get(i).getDate();
            commentString[i] = ratings.get(i).getCommento(); //String.valueOf(ratings.get(i).getRating());

            if(displayPreference.equals("stars"))
                ratingString[i] = displayRatingStars(ratings.get(i).getVoto());
            else
                ratingString[i] = String.valueOf(ratings.get(i).getVoto());

            Utils.getRatingAVG(r, i, context, 1, displayPreference); //prendo il rating AVG del numero corrente
            i++;
        }

    }

    /* CUSTOM LIST VIEW */
    /*static class MyAdapter extends ArrayAdapter<String> {
        Context context;
        String[] rPhoneNumber;
        String[] rDate;
        String[] rComment;
        String[] rRating;
        String[] rRatingAVG;

        MyAdapter(Context context, String[] phoneNumber, String[] date, String[] comment, String[] ratings, String[] ratingsAVG){
            super(context,R.layout.rows,R.id.phoneNumber, phoneNumber);
            this.context = context;
            this.rPhoneNumber = phoneNumber;
            this.rDate = date;
            this.rComment = comment;
            this.rRating = ratings;
            this.rRatingAVG = ratingsAVG;
        }

        @SuppressLint("SetTextI18n")
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder")
            View row = layoutInflater.inflate(R.layout.rows, parent, false);
            TextView phoneNumberView = row.findViewById(R.id.phoneNumber);
            TextView dateView = row.findViewById(R.id.date);
            TextView commentView = row.findViewById(R.id.comment);
            TextView ratingView = row.findViewById(R.id.rating);
            TextView ratingAVGView = row.findViewById(R.id.ratingAVG);

            String actualNumber = rPhoneNumber[position];

            for (Contact c : contacts){
                //scorro tutti i contatti che sono riuscito a leggere dalla rubrica
                Log.d("CONTACTS: ","confronto :'"+c.getPhone()+"'=='"+actualNumber+"'");
                if(c.getPhone().equals(actualNumber)){
                    //ho trovato un numero in rubrica !
                    //scrivo il nome e non il numero!
                    actualNumber = c.getName();
                    Log.d("CONTACTS: ","scrivo :'"+c.getName()+"'");
                }
            }

            phoneNumberView.setText(actualNumber);
            dateView.setText(rDate[position]);
            if(rComment[position].equals("")){
                commentView.setText("No comment");
            }
            else commentView.setText(rComment[position]);

            if(rRating[position].equals("-1.0")){
                ratingView.setText("Current rating: - ");
            }
            else ratingView.setText("Current rating: "+rRating[position]);

            ratingAVGView.setText("AVG rating: "+rRatingAVG[position]);

            return row;
        }
    }*/

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
