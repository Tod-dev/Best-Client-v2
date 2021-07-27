package it.bestclient.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import it.bestclient.android.RatingModel.Rating;
import it.bestclient.android.RatingModel.RatingCallLog;
import it.bestclient.android.components.Contact;
import it.bestclient.android.components.RowAdapter;

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
    //public static final int CONTATTI = 5;

    public static RecyclerView recyclerView;
    Context context;
    public static String[] phoneNumbers;
    public static double[] ratingDouble;
    public static double[] ratingAVGDouble;
    @SuppressLint("StaticFieldLeak")
    public static RowAdapter arrayAdapter;
    public static List<Contact> contacts = new ArrayList<>();
    public static Map<String, String> contactMap = new HashMap<>();
    //static final int MAX_ITEMS = 100;
    public static List<Rating> ratings = new ArrayList<>();

    @SuppressLint({"NonConstantResourceId", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*ActionBar set title*/
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.home);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_account_circle_24_white);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        context = this;
        recyclerView = findViewById(R.id.list);
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

    }

    public static void showRatings(Context context){
        if (ratings != null) {
            Utils.getDataFromDB(context, ratings);
            ratingToString(ratings, context);
            /* INSERT ALL THE RATINGS IN THE LISTVIEW */
            arrayAdapter = new RowAdapter((Activity)context, context, phoneNumbers, ratingDouble, ratingAVGDouble);
            recyclerView.setAdapter(arrayAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }

    }

    public static void showRatings(Context context, List<Rating> ratingList){
        if (ratingList != null) {
            Utils.getDataFromDB(context, ratingList);
            ratingToString(ratingList, context);
            /* INSERT ALL THE RATINGS IN THE LISTVIEW */
            arrayAdapter = new RowAdapter((Activity)context, context, phoneNumbers, ratingDouble, ratingAVGDouble);
            recyclerView.setAdapter(arrayAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }


    /*Menu creation -> add button refresh*/
    @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables"})
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        menu.findItem(R.id.searchBtn).setVisible(true);

        MenuItem search = menu.findItem(R.id.searchBtn);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setBackgroundColor(R.color.white);
        searchView.setBackground(this.getDrawable(R.drawable.sfondo_trasparente));
        searchView.setQueryHint("Cerca...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(ratings != null && ratings.size() > 0){
                    arrayAdapter.filter(newText, context);
                    return true;
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(HomeActivity.this, UserActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.to_left_in, R.anim.to_right_out);
            return true;
        }

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
                Toast.makeText(this, "L'app non ha accesso al registro delle chiamate, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "L'app non ha accesso al registro delle chiamate, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
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
                if(contactMap.containsKey(number)) ratingsRet.add(new Rating(number, contactMap.get(number)));
                else ratingsRet.add(new Rating(number));

                count++;

                if(count == 20){
                    //ogni 20 fa il refresh della lista
                    showRatings(context, ratingsRet);
                    count = 0;
                }
            }
        }
        c.close();
        
        return ratingsRet;
    }
    
    public List<Rating> getRatingContacts(){
        List<Rating> ratingsRet = new ArrayList<>();
        int count = 0;
        for(Contact c: contacts){
            ratingsRet.add(new Rating(c.getPhone(), c.getName()));
            count++;
            if(count == 20){
                //ogni 20 fa il refresh della lista
                showRatings(context, ratingsRet);
                count = 0;
            }
        }

        return ratingsRet;
    }


    public static void ratingToString(List<Rating> ratings, Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String displayPreference = preferences.getString("Display Rating", "number");

        phoneNumbers = new String[ratings.size()];
        ratingDouble = new double[ratings.size()];
        ratingAVGDouble = new double[ratings.size()];

        int i = 0;
        for(Rating r : ratings){
            phoneNumbers[i] = ratings.get(i).getNumero();

            ratingDouble[i] = ratings.get(i).getVoto();

            Utils.getRatingAVG(context, r, i, displayPreference); //prendo il rating AVG del numero corrente
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
            overridePendingTransition(R.anim.to_left_in, R.anim.to_right_out);
        }

    }

}
