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
    SharedPreferences.Editor editor;
    @SuppressLint("StaticFieldLeak")
    //static ListView listView;

    public static final int CHIAMATE_ENTRATA = 0;
    public static final int CHIAMATE_USCITA = 1;
    public static final int ULTIME_24H = 2;
    public static final int ULTIME_48H = 3;
    public static final int NO_FILTER = 4;
    public static final int CONTATTI = 5;

    static RecyclerView recyclerView;
    static DBhelper myDBhelper;
    static String[] phoneNumbers;
    static String[] ratingString;
    static String[] ratingAVGString;
    //List<RatingAVGOnDB> allRatings = new ArrayList<>();
    public static List<Contact> contacts = new ArrayList<>();
    public static Map<String, String> contactMap = null;
    static final int MAX_ITEMS = 100;
    public static List<Rating> ratings = new ArrayList<>();
    //final String uri = "http://worldtimeapi.org/api/timezone/Europe/Rome";

    @SuppressLint({"NonConstantResourceId", "CommitPrefEdits"})
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
        sp = this.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        editor = sp.edit();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            ContentResolver contentResolver = getContentResolver();
            contacts = fetchContacts(contentResolver,this);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED){
            ratings = getCallLog();
            showRatings(this);
        }
        else{
            Toast.makeText(this, "L'app non ha accesso al registro delle chiamate, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
        }
    }

    public void showRatings(Context context){
        if (ratings != null) {
            Utils.getDataFromDB(this, ratings);
            ratingToString(ratings, this);
            /* INSERT ALL THE RATINGS IN THE LISTVIEW */
            RowAdapter arrayAdapter = new RowAdapter(context, phoneNumbers, ratingString, ratingAVGString);
            recyclerView.setAdapter(arrayAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }


    /*Menu creation -> add button refresh*/
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.uscita) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED){
                editor.putString("scelta", String.valueOf(CHIAMATE_USCITA));
                editor.putString("filter", String.valueOf(NO_FILTER));
                editor.apply();
                ratings = getCallLog();
                showRatings(this);
                return true;
            }
            else{
                Toast.makeText(this, "L'app non ha accesso al registro delle chiamate, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (item.getItemId() == R.id.entrata) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                editor.putString("scelta", String.valueOf(CHIAMATE_ENTRATA));
                editor.putString("filter", String.valueOf(NO_FILTER));
                editor.apply();
                ratings = getCallLog();
                showRatings(this);
                return true;
            }
            else{
                Toast.makeText(this, "L'app non ha accesso al registro delle chiamate, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (item.getItemId() == R.id.contatti) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                ratings = getRatingContacts();
                showRatings(this);
                return true;
            }
            else{
                Toast.makeText(this, "L'app non ha accesso ai contatti, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
                return false;
            }

        }

        if (item.getItemId() == R.id.ultime24h) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                editor.putString("filter", String.valueOf(ULTIME_24H));
                editor.apply();
                ratings = getCallLog();
                showRatings(this);
                return true;
            }
            else{
                Toast.makeText(this, "L'app non ha accesso ai contatti, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (item.getItemId() == R.id.ultime48h) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                editor.putString("filter", String.valueOf(ULTIME_48H));
                editor.apply();
                ratings = getCallLog();
                showRatings(this);
                return true;
            }
            else{
                Toast.makeText(this, "L'app non ha accesso ai contatti, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return false;
    }

    
    public List<Rating> getCallLog(){
        int scelta = Integer.parseInt(sp.getString("scelta", String.valueOf(CHIAMATE_ENTRATA)));
        /*GET CURSOR FOR THE CALLS LOG*/
        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date", "type"}, null, null, "date DESC");

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        int filter = Integer.parseInt(sp.getString("filter", String.valueOf(HomeActivity.NO_FILTER)));

        List<Rating> ratingsRet = new ArrayList<>();
        List<RatingCallLog> ratingCallLogs = new ArrayList<>();

        Date curDate = Calendar.getInstance().getTime();
        /*READ CALLS LOG, LINE BY LINE*/
        int count = 0;
        while(c.moveToNext()){
            //Log.d("i, array:  ", ""+i + Arrays.toString(ratings.toArray()));
            count++;
            boolean skip = false;

            String type = c.getString(c.getColumnIndex(CallLog.Calls.TYPE));
            int typeNumber = Integer.parseInt(type);

            if((scelta == CHIAMATE_ENTRATA && typeNumber == CallLog.Calls.OUTGOING_TYPE) ||
                    (scelta == CHIAMATE_USCITA && typeNumber != CallLog.Calls.OUTGOING_TYPE)) continue;

            /*NEW RATING*/
            String number = c.getString(colNumber);
            Date date = new Date(Long.parseLong(c.getString(colDate)));

            RatingCallLog check = new RatingCallLog(number,date);

            long diffInMillies = Math.abs(date.getTime() - curDate.getTime());
            long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            /*FILTER in case settings say lastN*/
            switch (filter) {
                case ULTIME_24H: {
                    if (diff > 24) skip = true;
                    break;
                }
                case ULTIME_48H: {
                    if (diff > 48) skip = true;
                    break;
                }
                default:{
                    break;
                }
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

    @Override
    protected void onStart() {
        super.onStart();

        String email = sp.getString("email", "");
        String password = sp.getString("password", "");

        if (email.equals("") || password.equals("")) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        }

    }

}
