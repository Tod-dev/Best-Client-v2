package it.bestclient.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import it.bestclient.android.DB.DBhelper;
import it.bestclient.android.RatingModel.RatingLocal;
import it.bestclient.android.components.Contact;
import it.bestclient.android.components.RowAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static it.bestclient.android.Utils.displayRatingStars;

public class ContactsActivity extends AppCompatActivity {
    SharedPreferences sp;
    static String[] name;
    static String[] phoneNumber;
    static String[] commentString;
    static String[] ratingString;
    static String[] ratingAVGString;
    @SuppressLint("StaticFieldLeak")
    static ListView listView;
    static DBhelper myDBhelper;
    static String uid;
    //static Date currentDate;   //data corrente

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        /*ActionBar set title*/
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.contacts);
        }

        //salvo l'uid dell'utente che sta valutando
        sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        uid = sp.getString("uid", "");

        listView = findViewById(R.id.listcontacts);
        myDBhelper = new DBhelper(this);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            showRatings(this);
        }
        else Toast.makeText(this, "This app hasn't access to phone numbers, allow in settings", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showRatings(Context context){
        //prendo tutti i contatti dalla rubrica

        List<RatingLocal> ratings;
        ratings = getAllRatings();

        if(ratings != null){
            ratingToString(ratings, context);
        }

        //mostro la lista
        RowAdapter arrayAdapter = new RowAdapter(context, name, phoneNumber, commentString, ratingString, ratingAVGString);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            //currentDate = Calendar.getInstance().getTime();   //data corrente
            RatingLocal r = ratings.get(position);
            Utils.showDialog(context, 3, r, myDBhelper, uid);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<RatingLocal> getAllRatings(){
        //cerco i contatti a cui ho già dato una valutazione, avranno rating = -3
        Cursor data = myDBhelper.getData();
        List<RatingLocal> alreadyInserted = new ArrayList<>();
        while(data.moveToNext()){
            String cell = data.getString(data.getColumnIndex(DBhelper.COL_CELL));
            String date = data.getString(data.getColumnIndex(DBhelper.COL_DATE));
            float rating = data.getFloat(data.getColumnIndex(DBhelper.COL_RATING));
            String commento = data.getString(data.getColumnIndex(DBhelper.COL_COMMENT));
            String firebase_key = data.getString(data.getColumnIndex(DBhelper.COL_FIREBASE_KEY));

            if (date.equals(""))
                alreadyInserted.add(new RatingLocal(cell,null,rating,commento,firebase_key));
        }

        List<RatingLocal> ratings = new ArrayList<>();
        for(Contact c: HomeActivity.contacts){
            boolean presente = false;
            for(RatingLocal r: alreadyInserted){
                if(r.getNumero().equals(c.getPhone())){
                    presente = true;
                    ratings.add(r);
                    break;
                }
            }

            //aggiungo alla lista di rating i contatti che non sono stati valutati
            if(!presente){
                ratings.add(new RatingLocal(c.getPhone(), null));
            }
        }

        return ratings;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void ratingToString(List<RatingLocal> ratings, Context context){
        /*
         *save all the data in 3 parallel arrays of String data
         *in order to create the listView easily
         */

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String displayPreference = preferences.getString("Display Rating", "number");

        int size = HomeActivity.contacts.size();
        name = new String[size];
        phoneNumber = new String[size];
        commentString = new String[size];
        ratingString = new String[size];
        ratingAVGString = new String[size];

        int i = 0;
        for(RatingLocal r : ratings){
            name[i] = HomeActivity.contacts.get(i).getName();
            phoneNumber[i] = ratings.get(i).getNumero();
            if(r.getVoto() == -1){
                commentString[i] = "";
                ratingString[i] = "-1.0";
            }
            else{
                commentString[i] = ratings.get(i).getCommento();
                if(displayPreference.equals("stars"))
                    ratingString[i] = displayRatingStars(ratings.get(i).getVoto());
                else
                    ratingString[i] = String.valueOf(ratings.get(i).getVoto());
            }

            Utils.getRatingAVG(r, i, context, 2, displayPreference); //prendo il rating AVG del numero corrente
            i++;
        }

    }

    /*
    public static String getKey(RatingLocal r, List<RatingLocal> alreadyInserted){
        for(RatingLocal inserted: alreadyInserted){
            if(r.getNumero().equals(inserted.getNumero())) return inserted.get_firebase_key();
        }

        return null;
    }
*/
    /*static class MyAdapter extends ArrayAdapter<String> {
        Context context;
        String[] rPhoneNumber;
        String[] name;
        String[] commentString;
        String [] ratingString;
        String [] ratingAVGString;

        MyAdapter(Context context, String[] name, String[] phoneNumber,String[] commentString,String[]ratingString,String[]ratingAVGString){
            super(context,R.layout.rows_contacts,R.id.phone, phoneNumber);
            this.context = context;
            this.rPhoneNumber = phoneNumber;
            this.name = name;
            this.commentString = commentString;
            this.ratingString = ratingString;
            this.ratingAVGString = ratingAVGString;
        }

        @SuppressLint("SetTextI18n")
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder")
            View row = layoutInflater.inflate(R.layout.rows_contacts, parent, false);
            TextView nameView = row.findViewById(R.id.name);
            TextView phoneNumberView = row.findViewById(R.id.phone);

            nameView.setText(name[position]);
            phoneNumberView.setText(phoneNumber[position]);

            return row;
        }
    }*/
}