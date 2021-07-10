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

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            showRatings(this);
        }
        else Toast.makeText(this, "This app hasn't access to phone numbers, allow in settings", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showRatings(Context context){
        myDBhelper = new DBhelper(context);
        //prendo tutti i contatti dalla rubrica

        //cerco i contatti a cui ho già dato una valutazione, avranno rating = -3
        Cursor data = myDBhelper.getData();
        List<RatingLocal> alreadyInserted = new ArrayList<>();
        while(data.moveToNext()){
            String cell = data.getString(data.getColumnIndex(DBhelper.COL_CELL));
            String date = data.getString(data.getColumnIndex(DBhelper.COL_DATE));
            float rating = data.getFloat(data.getColumnIndex(DBhelper.COL_RATING));
            String commento = data.getString(data.getColumnIndex(DBhelper.COL_COMMENT));
            String firebase_key = data.getString(data.getColumnIndex(DBhelper.COL_FIREBASE_KEY));

            //alreadyInserted.add(new RatingLocal(cell, Rating.formatter.parse(date)));
            if (date.equals(""))
                alreadyInserted.add(new RatingLocal(cell,null,rating,commento,firebase_key));
        }


        int size = HomeActivity.contacts.size();
        name = new String[size];
        phoneNumber = new String[ size];
        commentString = new String[ size];
        ratingString = new String[ size];
        ratingAVGString = new String[ size];

        Arrays.fill(name,"");
        Arrays.fill(phoneNumber,"");
        Arrays.fill(commentString,"");
        Arrays.fill(ratingString,"");
        Arrays.fill(ratingAVGString,"");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String displayPreference = preferences.getString("Display Rating", "number");

        List<RatingLocal> finalRatings = new ArrayList<>();
        //Per ogni contatto controllo se è già stato inserito
        int index = 0;
        double voto;
        String firebaseKey="";
        for (Contact c :  HomeActivity.contacts) {
            voto = -5;//rating non inserito
            name[index] = c.getName();
            phoneNumber[index] = c.getPhone();

            /*if(phoneNumber[index].isEmpty()){//salto in questo caso, non voglio un contatto vuoto
                continue;
            }*/

            for(RatingLocal r : alreadyInserted){
                if (phoneNumber[index].equals(r.getNumero())) {
                    commentString[index]=r.getCommento();
                    if(displayPreference.equals("stars"))
                        ratingString[index] = displayRatingStars(r.getVoto());
                    else
                        ratingString[index] = String.valueOf(r.getVoto());
                    firebaseKey = r.get_firebase_key();
                    voto = r.getVoto();
                }
            }
            RatingLocal k;
            if (voto == -5){
                //rating non ancora inserito
                k = new RatingLocal(phoneNumber[index],null);
            }else{
                //rating già inserito per questo contatto
                k = new RatingLocal(phoneNumber[index],null,voto,commentString[index],firebaseKey);
            }
            Utils.getRatingAVG(k, index, context, 2, displayPreference);
            finalRatings.add(k);
            index++;
        }

        //mostro la lista
        RowAdapter arrayAdapter = new RowAdapter(context, name, phoneNumber, commentString, ratingString, ratingAVGString);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            //currentDate = Calendar.getInstance().getTime();   //data corrente
            RatingLocal r = finalRatings.get(position);
            Utils.showDialog(context, 3, r, myDBhelper, uid);
        });
    }/*
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