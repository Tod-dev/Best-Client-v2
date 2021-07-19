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
import it.bestclient.android.RatingModel.RatingCallLog;
import it.bestclient.android.components.Contact;
import it.bestclient.android.components.RowAdapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static it.bestclient.android.Utils.displayRatingStars;
import static it.bestclient.android.Utils.fetchContacts;


@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeActivity extends AppCompatActivity {

    // GoogleSignInClient mGoogleSignInClient;
    SharedPreferences sp;
    @SuppressLint("StaticFieldLeak")
    //static ListView listView;

    final int CHIAMATE_ENTRATA = 0;
    final int CHIAMATE_USCITA = 1;
    final int ULTIME_24H = 2;
    final int ULTIME_48H = 3;
    final int CONTATTI = 4;

    static RecyclerView recyclerView;
    static DBhelper myDBhelper;
    static String[] phoneNumbers;
    static String[] ratingString;
    static String[] ratingAVGString;
    //List<RatingAVGOnDB> allRatings = new ArrayList<>();
    public static List<Contact> contacts = null;
    public static Map<String, String> contactMap = null;
    static final int MAX_ITEMS = 100;
    public static List<Rating> ratings = new ArrayList<>();
    //final String uri = "http://worldtimeapi.org/api/timezone/Europe/Rome";

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*ActionBar set title*/
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.home);
        }

        recyclerView = findViewById(R.id.list);
        myDBhelper = new DBhelper(this);

        /*If we got Permissions -> fetch ratings*/
        if (checkPermissions()) {
            showRatings(this, CHIAMATE_ENTRATA);
        }
    }

    public boolean checkPermissions(){
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)){
            ContentResolver contentResolver = getContentResolver();
            contacts = fetchContacts(contentResolver,this);

            return true;
        }

        Toast.makeText(this, "This app hasn't access to phone numbers, call log or contacts, allow in settings", Toast.LENGTH_LONG).show();
        return false;
    }

    public void showRatings(Context context, int scelta){
        /* GET ALL RATINGS */
        switch(scelta){
            case CHIAMATE_ENTRATA:{
                ratings = getCallLog();
                break;
            }
            case CHIAMATE_USCITA:{
                ratings = getCallLog();
                break;
            }

            case ULTIME_24H:{
                ratings = getCallLog();
                break;
            }

            case ULTIME_48H:{
                ratings = getCallLog();
                break;
            }

            case CONTATTI:{
                ContentResolver contentResolver = getContentResolver();
                contacts = fetchContacts(contentResolver,this);
                ratings = getRatingContacts();
                break;
            }
            default:{
                break;
            }
        }


        if (ratings != null) {
            Utils.getDataFromDB(this, ratings);
            ratingToString(ratings, this);
            /* INSERT ALL THE RATINGS IN THE LISTVIEW */
            RowAdapter arrayAdapter = new RowAdapter(context, phoneNumbers, ratingString, ratingAVGString);
            recyclerView.setAdapter(arrayAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        //List<Rating> finalRatings = ratings;
        /*recyclerView.setOnItemClickListener((parent, view, i1, id) -> {
            Rating k = finalRatings.get(i1);

            Utils.showDialog(context, 2, k, myDBhelper, uid);
        });*/
    }


    /*Menu creation -> add button refresh*/
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.uscita) {
            showRatings(this, CHIAMATE_USCITA);
            return true;
        }

        if (item.getItemId() == R.id.entrata) {
            showRatings(this, CHIAMATE_ENTRATA);
            return true;
        }

        if (item.getItemId() == R.id.contatti) {
            showRatings(this, CONTATTI);
            return true;
        }

        if (item.getItemId() == R.id.ultime24h) {
            showRatings(this, ULTIME_24H);
            return true;
        }

        if (item.getItemId() == R.id.ultime48h) {
            showRatings(this, ULTIME_48H);
            return true;
        }

        return false;
    }

    
    public List<Rating> getCallLog(){
        /*GET CURSOR FOR THE CALLS LOG*/
        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date"}, null, null, "date DESC");

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String notificationPreference = preferences.getString("Last Calls", "None");

        List<Rating> ratingsRet = new ArrayList<>();
        List<RatingCallLog> ratingCallLogs = new ArrayList<>();

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

            RatingCallLog check = new RatingCallLog(number,date);

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
            for(RatingCallLog r: ratingCallLogs){
                boolean res = r.group_by(check);
                //Log.d("CHECK_CONFRONTO: ", ""+res);
                if(res){
                    checkIfExist = true;
                    break;
                }
            }

            /*ADD A NEW RATING TO THE LIST*/
            if(!checkIfExist){
                ratingCallLogs.add(new RatingCallLog(number, date));
                ratingsRet.add(new Rating(number));
            }
        }
        c.close();
        
        return ratingsRet;
    }
    
    public List<Rating> getRatingContacts(){
        List<Rating> ratingsRet = new ArrayList<>();
        for(Contact c: contacts){
            ratingsRet.add(new Rating(c.getPhone()));
        }

        return ratingsRet;
    }


    public void ratingToString(List<Rating> ratings, Context context){
        /*
        *save all the data in 3 parallel arrays of String data
        *in order to create the listView easily
        */

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String displayPreference = preferences.getString("Display Rating", "number");

        phoneNumbers = new String[ratings.size()];
        ratingString = new String[ratings.size()];
        ratingAVGString = new String[ratings.size()];

        int i = 0;
        for(Rating r : ratings){
            phoneNumbers[i] = ratings.get(i).getNumero();

            if(displayPreference.equals("stars"))
                ratingString[i] = displayRatingStars(ratings.get(i).getVoto());
            else
                ratingString[i] = String.valueOf(ratings.get(i).getVoto());

            Utils.getRatingAVG(this, r, i, displayPreference); //prendo il rating AVG del numero corrente
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
