package com.example.progettobiancotodaro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.progettobiancotodaro.DB.DBhelper;
import com.example.progettobiancotodaro.RatingModel.Rating;
import com.example.progettobiancotodaro.RatingModel.RatingBigOnDB;
import com.example.progettobiancotodaro.RatingModel.RatingLocal;
import com.example.progettobiancotodaro.components.Contact;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.example.progettobiancotodaro.Utils.updateDB;

public class ContactsActivity extends AppCompatActivity {
    SharedPreferences sp;
    String[] name;
    String[] phoneNumber;
    ListView listView;
    DBhelper myDBhelper;
    String uid;
    Date currentDate;   //data corrente

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
        this.uid = sp.getString("uid", "");

        listView = findViewById(R.id.listcontacts);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            showRatings();
        }
        else Toast.makeText(this, "This app hasn't access to phone numbers, allow in settings", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showRatings(){
        myDBhelper = new DBhelper(this);
        //prendo tutti i contatti dalla rubrica

        //cerco i contatti a cui ho già dato una valutazione, avranno rating = -3
        Cursor data = myDBhelper.getData();
        List<RatingLocal> alreadyInserted = new ArrayList<>();
        while(data.moveToNext()){
            String cell = data.getString(data.getColumnIndex("number"));
            String date = data.getString(data.getColumnIndex("data"));
            float rating = data.getFloat(data.getColumnIndex("rating"));
            if(rating == -3) {
                try {
                    alreadyInserted.add(new RatingLocal(cell, Rating.formatter.parse(date)));
                } catch (ParseException e) {
                    //errore nel parsing della data
                    e.printStackTrace();
                }
            }
        }

        //creo un arrayList listData che conterrà solo i contatti per cui non è stato inserito un rating
        for(Iterator<Contact> i = HomeActivity.contacts.iterator(); i.hasNext();){
            boolean presente = false;
            Contact c = (Contact) i.next();
            for(RatingLocal r: alreadyInserted){
                if(c.getPhone().equals(r.getNumero())){
                    presente = true;
                    break;
                }
            }
            if(presente) i.remove();    //rimuovo il contatto già inserito
        }

        //costruisco la lista dei contatti da visualizzare, ovvero quelli che non sono già stati valutati

        name = new String[ HomeActivity.contacts.size()];
        phoneNumber = new String[ HomeActivity.contacts.size()];

        //salvo i nomi e i numeri di telefono nei vettori
        int index = 0;
        for (Contact c :  HomeActivity.contacts) {
            name[index] = c.getName();
            phoneNumber[index] = c.getPhone();
            index++;
        }

        //mostro la lista
        ContactsActivity.MyAdapter arrayAdapter = new ContactsActivity.MyAdapter(this, name, phoneNumber);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();

            builder.setTitle(R.string.dialogMessage);
            View viewDialog = inflater.inflate(R.layout.rating_stars, null);
            TextInputLayout comment = viewDialog.findViewById(R.id.comment);
            TextInputEditText commentText = viewDialog.findViewById(R.id.commentText);
            RatingBar ratingbar = viewDialog.findViewById(R.id.ratingStars);
            ImageView deleteButton = viewDialog.findViewById(R.id.delete);
            deleteButton.setVisibility(View.INVISIBLE);
            ImageView commentButton = viewDialog.findViewById(R.id.commentLogo);

            builder.setView(viewDialog).setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                currentDate = Calendar.getInstance().getTime();   //data corrente
                float rating = ratingbar.getRating(); //valutazione inserita
                String commento = commentText.getText().toString();  //commento inserito
                String number = phoneNumber[position];

                RatingLocal r = new RatingLocal(number, currentDate, rating, commento);

                UpdateData(r);

                //refresh della lista
                showRatings();

                dialog.dismiss();
            }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            commentButton.setOnClickListener(v -> {
                comment.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.INVISIBLE);
            });
        });
    }

    private void UpdateData(RatingLocal r){
        /* AGGIORNA I DATI SUL DB SQLITE E SU FIREBASE */
        RatingBigOnDB remoteRating = new RatingBigOnDB(uid,currentDate,r.getVoto(),r.getNumero(),r.getCommento());

        updateDB(remoteRating);

        r.setVoto(-3);  //imposto il rating in locale a -3 per segnalare il fatto che è stato inserito dai contatti

        boolean insertData = myDBhelper.addData(r.getNumero(),r.getDate(),r.getVoto(), r.getCommento());

        if (insertData) {
            Toast.makeText(this,"Valutazione inserita correttamente!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"Qualcosa è andato storto :(", Toast.LENGTH_SHORT).show();
        }

    }


    class MyAdapter extends ArrayAdapter<String> {
        Context context;
        String[] rPhoneNumber;
        String[] name;

        MyAdapter(Context context, String[] name, String[] phoneNumber){
            super(context,R.layout.rows_contacts,R.id.phone, phoneNumber);
            this.context = context;
            this.rPhoneNumber = phoneNumber;
            this.name = name;
        }

        @SuppressLint("SetTextI18n")
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder")
            View row = layoutInflater.inflate(R.layout.rows_contacts, parent, false);
            TextView nameView = row.findViewById(R.id.name);
            TextView phoneNumberView = row.findViewById(R.id.phone);

            nameView.setText(name[position]);
            phoneNumberView.setText(phoneNumber[position]);

            return row;
        }
    }
}