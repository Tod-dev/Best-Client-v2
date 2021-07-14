package it.bestclient.android;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import it.bestclient.android.DB.DBhelper;
import it.bestclient.android.RatingModel.RatingBigOnDB;
import it.bestclient.android.RatingModel.RatingLocal;
import it.bestclient.android.components.Contact;
import it.bestclient.android.components.RowAdapter;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Utils {
    /**
     * Classe che contiene metodi statici per manipolare i dati nell'app
     */

    public static void toastMessage(String message, Context k){
        Toast.makeText(k,message, Toast.LENGTH_SHORT).show();
    }

    /*POST NEW RATING TO RATINGBIG TABLE ON FIREBASE (REST)*/
    public static String updateDB(RatingBigOnDB r, String key){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("ratingBig");
        Log.d("ratingonDB:", "CHIAVE RICEVUTA KEY: "+ key); //ratingBig + KEY
        if(key.equals("")){
            //se non ho una chiave ne creo una nuova
            key = mDatabase.push().getKey();
        }
        Log.d("ratingonDB:", "Sto USANDO IL DB: scrivo: "+ r.toString()+ " KEY: "+ key); //ratingBig + KEY
        //write new RATINGBIG
        mDatabase.child(key).setValue(r);
        return key;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<Contact> fetchContacts(ContentResolver contentResolver, Context k){
        Set<Contact> contacts = new TreeSet<>();    //con un set impedisco l'inserimento di contatti duplicati
        HomeActivity.contactMap = new HashMap<>();   //numeri già inseriti

        /*controllo se sono in rubrica*/
        if ((ContextCompat.checkSelfPermission(k, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            //solo se ho i permessi di accesso alla rubrica
            try (Cursor cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI
                    , null
                    , null
                    , null
                    , null)) {
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        Contact contact = new Contact();
                        String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        contact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                        int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                        if (hasPhoneNumber > 0) {
                            Cursor phoneCursor = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                    , null
                                    , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                                    , new String[]{contact_id}
                                    , null);
                            if (phoneCursor != null) {
                                phoneCursor.moveToNext();
                                String phoneinContact = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                if(!HomeActivity.contactMap.containsKey(phoneinContact)) {
                                    //non è stato trovato un contatto con stesso numero
                                    contact.setPhone(filterOnlyDigits(phoneinContact));
                                    contacts.add(contact);
                                    HomeActivity.contactMap.put(phoneinContact, phoneinContact);
                                }
                            }
                            if (phoneCursor != null) phoneCursor.close();
                        }
                        //contact.image = ContactPhoto(contact_id);
                    }
                    //ho tutti i contatti in contacts
                }
            } catch (Exception ignored) {
            }

        }
        //Collections.sort(contacts);
        return new ArrayList<>(contacts);
        //return ret;
    }

    /*FILTER ONLY RATINGS NOT ELIMINATED*/
    static void filtroNonMeno2(List<RatingLocal> r){
        for(Iterator<RatingLocal> k = r.iterator(); k.hasNext();){
            if(k.next().getVoto() == -2){
                k.remove();
            }
        }
    }

    public static String filterOnlyDigits(String s){
        StringBuilder sb = new StringBuilder(s);
        for(int i =0;i<sb.length();i++){
            if(!(sb.charAt(i) >= '0' && sb.charAt(i) <= '9')){
                //not digit
                sb.deleteCharAt(i);
                i--;
            }
        }
        return sb.toString();
    }

    public static String displayRatingStars(double rating){
        StringBuilder s= new StringBuilder();
        double resto = rating-Math.floor(rating);
        for(long i = 0 ; i < Math.floor(rating);i++){
            s.append("⭐");
        }
        if(resto > 0){
            s.append("☆");
        }
        return s.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showDialog(Context context, int type, RatingLocal r){
        /* show a dialog box when a user click on a rating! */
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        if(type == 1 ) { //Dialog cancella un elemento
            builder.setTitle(R.string.dialogMessage2);
            View viewDialog = inflater.inflate(R.layout.delete_rating, null);
            builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        float nuovoRating = -2;
                        r.setVoto(nuovoRating);
                        try {
                            UpdateData(context, r, false);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        HomeActivity.showRatings(context);
                        //refreshView(r, listView);

                        //ratings.get(i1).setRating(rating.getRating());
                        //Toast.makeText(AddRating.this,Float.toString(ratingbar.getRating()),Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else { //Dialog dai un voto / cancella un elemento
            builder.setTitle(R.string.dialogMessage);
            View viewDialog = inflater.inflate(R.layout.rating_stars, null);
            TextInputLayout comment = viewDialog.findViewById(R.id.comment);
            TextInputEditText commentText = viewDialog.findViewById(R.id.commentText);
            RatingBar ratingbar = viewDialog.findViewById(R.id.ratingStars);
            ImageView deleteButton = viewDialog.findViewById(R.id.delete);
            ImageView commentButton = viewDialog.findViewById(R.id.commentLogo);

            if(!r.getCommento().equals("")){
                commentText.setText(r.getCommento());
            }

            if(r.getVoto() > 0){
                ratingbar.setRating((float) r.getVoto());
            }

            builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        float nuovoRating =  ratingbar.getRating();


                        r.setCommento(removeQuotes(comment.getEditText().getText().toString()));


                        if(nuovoRating != 0){
                            //se ho inserito un rating modifico il db firebase
                            r.setVoto(nuovoRating);
                            try {
                                UpdateData(context, r, true);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            try {
                                UpdateData(context, r, false);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }

                        if(type == 2)
                            HomeActivity.showRatings(context);
                        else
                            ContactsActivity.showRatings(context);
                        //refreshView(r, listView);

                        //ratings.get(i1).setRating(rating.getRating());
                        //Toast.makeText(AddRating.this,Float.toString(ratingbar.getRating()),Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());

            if(type == 3){
                //nei contatti rendo invisibile il tasto di eliminazione del rating
                deleteButton.setVisibility(View.INVISIBLE);
            }

            AlertDialog dialog = builder.create();
            dialog.show();
            deleteButton.setOnClickListener(v -> {
                dialog.dismiss();
                showDialog(context, 1, r);
            });
            commentButton.setOnClickListener(v -> {
                comment.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.INVISIBLE);
            });

        }
    }

    public static void UpdateData(Context context, RatingLocal r, boolean db) throws ParseException {
        /* AGGIORNA I DATI SUL DB SQLITE E SU FIREBASE */
        Date date = new Date();
        DBhelper myDBhelper = new DBhelper(context);
        String uid = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE).getString("uid", "");
        RatingBigOnDB remoteRating = new RatingBigOnDB(uid,date,r.getVoto(),r.getNumero(),r.getCommento());
        String key=r.get_firebase_key();
        if(db){
            key = updateDB(remoteRating,key);
            r.set_firebase_key(key);
        }

        /*if(r.getVoto() > 0){
            r.setVoto(-2);
        }*/

        int ret = myDBhelper.updateRating(r);
        if(ret == -1){
            boolean insertData = myDBhelper.addData(r);

            if (insertData) {
                toastMessage("Valutazione inserita correttamente!",context);
            } else {
                toastMessage("Qualcosa è andato storto :(",context);
            }
        }else{
            // toastMessage("Data Successfully Updated!");
            Log.d("DATA IN LOCALE", "UpdateData: ");
        }
    }

    public static String removeQuotes(String text) {

        return text.replaceAll("'", "");
    }

    public static void getRatingAVG(RatingLocal r, int index, Context context, int type, String preferences){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("ratingAVG").child(r.getNumero());
        //Log.d("CONTATCT-DEBUG",r.getNumero());
        //Log.d("CONTATCT-DEBUG-rating",r.toString());
        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() == null){
                    if(type == 1){
                        HomeActivity.ratingAVGString[index] = " - ";
                    }
                    else ContactsActivity.ratingAVGString[index] = " - ";

                }
                else{
                    if(!(r.getNumero().isEmpty())){
                        Log.d("CONTATCT-DEBUG",ratingsRef.toString()+" "+dataSnapshot.getValue()+" "+r.toString());
                        double val = dataSnapshot.getValue(Double.class);
                        val = Math.round(val*100.0)/100.0;  //arrotondo il rating a due cifre decimali
                        if(type == 1){
                            if(preferences.equals("stars"))
                                HomeActivity.ratingAVGString[index] = displayRatingStars(val);
                            else
                                HomeActivity.ratingAVGString[index] = String.valueOf(val);
                        }
                        else {
                            if(preferences.equals("stars"))
                                ContactsActivity.ratingAVGString[index] = displayRatingStars(val);
                            else
                                ContactsActivity.ratingAVGString[index] = String.valueOf(val);
                        }

                    }
                }

                if(type == 1){
                    RowAdapter arrayAdapter = new RowAdapter(context, HomeActivity.phoneNumbers, HomeActivity.dates, HomeActivity.commentString, HomeActivity.ratingString, HomeActivity.ratingAVGString);
                    HomeActivity.recyclerView.setAdapter(arrayAdapter);
                }
                else{
                    RowAdapter arrayAdapter = new RowAdapter(context, ContactsActivity.name, ContactsActivity.phoneNumber, ContactsActivity.commentString, ContactsActivity.ratingString, ContactsActivity.ratingAVGString);
                    ContactsActivity.recyclerView.setAdapter(arrayAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Main", "Failed to read value.", error.toException());
            }
        });
    }
}
