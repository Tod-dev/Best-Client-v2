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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import it.bestclient.android.DB.DBhelper;
import it.bestclient.android.RatingModel.Rating;
import it.bestclient.android.RatingModel.RatingLocal;
import it.bestclient.android.components.Contact;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static it.bestclient.android.Utils.fetchContacts;
import static it.bestclient.android.Utils.filtroNonMeno2;


@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeActivity extends AppCompatActivity {

    // GoogleSignInClient mGoogleSignInClient;
    Button ratingButton;
    SharedPreferences sp;
    @SuppressLint("StaticFieldLeak")
    static ListView listView;
    static DBhelper myDBhelper;
    static String[] phoneNumbers;
    static String[] dates;
    static String[] commentString;
    //List<RatingAVGOnDB> allRatings = new ArrayList<>();
    static String uid;
    public static List<Contact> contacts = null;
    static final int MAX_ITEMS = 100;
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
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;
                }
                case R.id.profileBtn:{
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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

        /*If we got Permissions -> fetch ratings*/
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)) {
            myDBhelper = new DBhelper(this);

            listView = findViewById(R.id.list);

            /*SET UID*/
            sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
            HomeActivity.uid = sp.getString("uid", "");
            //toastMessage(uid);
            ContentResolver contentResolver = getContentResolver();
            contacts = fetchContacts(contentResolver,this);
            for( Contact c : contacts)
                Log.d("CONTACTS: ",c.toString());
            showRatings(this);
        }
    }



    public static void showRatings(Context context){
        /* GET ALL RATINGS */
        List<RatingLocal> ratings = null;
        try {
            ratings = getAllRatings(context);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (ratings != null) {
            ratingToString(ratings);
        }

        /* INSERT ALL THE RATINGS IN THE LISTVIEW */
        HomeActivity.MyAdapter arrayAdapter = new HomeActivity.MyAdapter(context, phoneNumbers, dates, commentString);
        listView.setAdapter(arrayAdapter);
        List<RatingLocal> finalRatings = ratings;
        listView.setOnItemClickListener((parent, view, i1, id) -> {
            RatingLocal k = finalRatings.get(i1);

            /* IF I HAVE ALREADY INSERTED THAT RATING -> ASK IF YOU WANT TO DELETE IT */
            if (k.getVoto() != -1) {
                /*Dialog delete an element*/
                Utils.showDialog(context, 1, finalRatings.get(i1), myDBhelper, uid);
            } else {
                /*Dialog give a vote or delete an element*/
                Utils.showDialog(context, 2, finalRatings.get(i1), myDBhelper, uid);
            }
        });
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
            showRatings(this);
            return true;
        }

        if (item.getItemId() == R.id.contacts) {
            Intent intent = new Intent(HomeActivity.this, ContactsActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }





    /*EVERY TIME REFRESH BUTTON IS CLICKED -> REFRESH THE LIST OF RATINGS TO DISPLAY*/
    public void refreshView(List<RatingLocal> ratings, ListView listView){
        if(ratings != null && listView != null){
            filtroNonMeno2(ratings);
            ratingToString(ratings);
            HomeActivity.MyAdapter arrayAdapter = new HomeActivity.MyAdapter(this, phoneNumbers, dates, commentString);
            listView.setAdapter(arrayAdapter);
        }
    }




    public static List<RatingLocal> getAllRatings(Context context) throws ParseException {

        /*GET CURSOR FOR THE CALLS LOG*/
        Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date"}, null, null, "date DESC");

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String notificationPreference = preferences.getString("Last Calls", "None");

        List<RatingLocal> ratings = new ArrayList<>();

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
            for(RatingLocal r: ratings){
                boolean res = r.group_by(check);
                //Log.d("CHECK_CONFRONTO: ", ""+res);
                if(res){
                    checkIfExist = true;
                    break;
                }
            }

            /*ADD A NEW RATING TO THE LIST*/
            if(!checkIfExist){
                ratings.add(new RatingLocal(number, date));
            }
        }
        c.close();

        /*ALL THE CALL LOGS IN ratings list grouped by DATA(DAY,PHONE_NUMBER)*/

        /*GET DATA FROM SQL LITE DB (LOCAL) TO SEE IF I HAVE ALREADY INSERTED SOME RATINGS OR NOT*/
        Cursor data = myDBhelper.getData();
        List<RatingLocal> listData = new ArrayList<>();
        while(data.moveToNext()){
            String cell = data.getString(data.getColumnIndex("number"));
            String date = data.getString(data.getColumnIndex("data"));
            float rating = data.getFloat(data.getColumnIndex("rating"));
            String comment = data.getString(data.getColumnIndex("comment"));

            //IF rating = -3 ignora perchè si riferisce ad un contatto
            if(rating == -3) continue;

            /*IF NOT RATED (-1) or comment inserted -> comment to insert*/
            if(rating != -1 || !comment.equals(""))
                listData.add(new RatingLocal(cell, Rating.formatter.parse(date), rating, comment));
        }

        for(RatingLocal r: ratings){
            for(RatingLocal j: listData){
                if(r.group_by(j)){
                    /*write in the ratings list the rating if inserted*/
                    if(j.getVoto() != -1){
                        r.setVoto(j.getVoto());
                    }
                    /*write in the ratings list the last comment inserted*/
                    if(!j.getCommento().equals("")){
                        r.setCommento(j.getCommento());
                    }
                }
            }
        }

        //filtro solo != -2 (RATING ELIMINATO) -> Iterator
        filtroNonMeno2(ratings);


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
        return ratings;

    }


    public static void ratingToString(List<RatingLocal> ratings){
        /*
        *save all the data in 3 parallel arrays of String data
        *in order to create the listView easily
        */
        phoneNumbers = new String[ratings.size()];
        dates = new String[ratings.size()];
        commentString = new String[ratings.size()];

        int i = 0;
        for(RatingLocal ignored : ratings){
            phoneNumbers[i] = ratings.get(i).getNumero();
            dates[i] = ratings.get(i).getDate();
            commentString[i] = ratings.get(i).getCommento(); //String.valueOf(ratings.get(i).getRating());
            i++;
        }
    }

    /* CUSTOM LIST VIEW */
    static class MyAdapter extends ArrayAdapter<String> {
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
            LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder")
            View row = layoutInflater.inflate(R.layout.rows, parent, false);
            TextView phoneNumberView = row.findViewById(R.id.phoneNumber);
            TextView dateView = row.findViewById(R.id.date);
            TextView ratingView = row.findViewById(R.id.rating);

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
