package it.bestclient.android;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import it.bestclient.android.RatingModel.Rating;
import it.bestclient.android.RatingModel.RatingAVGOnDB;
import it.bestclient.android.RatingModel.RatingBigOnDB;
import it.bestclient.android.components.Contact;
import it.bestclient.android.components.RowAdapter;

public class Utils {
    /**
     * Classe che contiene metodi statici per manipolare i dati nell'app
     */
    /*COSTANTI FIREBASE*/
    public static final String USERS = "Users";
    public static final String VALUTAZIONI = "Valutazioni";

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
                                    HomeActivity.contactMap.put(contact.getPhone(), contact.getName());
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
//    static void filtroNonMeno2(List<RatingLocal> r){
//        for(Iterator<RatingLocal> k = r.iterator(); k.hasNext();){
//            if(k.next().getVoto() == -2){
//                k.remove();
//            }
//        }
//    }

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

    public static String removeQuotes(String text) {

        return text.replaceAll("'", "");
    }

    public static void getRatingAVG(Context context, Rating r, int index){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("ratingAVG").child(r.getNumero());

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() == null){
                    HomeActivity.ratingAVGDouble[index] = 0;
                    r.setCommentList("");
                    r.setVoto_medio(0);
                }
                else{
                    RatingAVGOnDB ratingAVGOnDB = dataSnapshot.getValue(RatingAVGOnDB.class);

                    double val = ratingAVGOnDB.getVotoMedio();
                    val = Math.round(val*100.0)/100.0;  //arrotondo il rating a due cifre decimali

                    HomeActivity.ratingAVGDouble[index] = val;

                    r.setVoto_medio(val);
                    r.setCommentList(ratingAVGOnDB.getCommentList());
                }

                if(r.getVoto() > 0 || r.getVoto_medio() > 0){
                    HomeActivity.logos[index] = R.drawable.logo_red;
                }

                RowAdapter arrayAdapter = new RowAdapter((Activity)context, context, HomeActivity.logos, HomeActivity.phoneNumbers, HomeActivity.ratingDouble, HomeActivity.ratingAVGDouble);
                HomeActivity.recyclerView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Main", "Failed to read value.", error.toException());
            }
        });
    }

    //Funzione che scarica la lista di valutazioni dell'utente collegato e le inserisce in ratingsOnDb
    public static void getDataFromDB(Context context){

        String uid = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE).getString("uid", "");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("Users").child(uid).child("Valutazioni");

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot d: dataSnapshot.getChildren()){
                    Rating r = d.getValue(Rating.class);
                    r.setNumero(d.getKey());

                    if(HomeActivity.contactMap.containsKey(r.getNumero())) r.setNome(HomeActivity.contactMap.get(r.getNumero()));
                    else r.setNome("");

                    HomeActivity.ratingsOnDb.put(r.getNumero(), r);
                }
                HomeActivity.checkDataOnDB = true;
                int scelta = Integer.parseInt(HomeActivity.sp.getString("scelta", String.valueOf(HomeActivity.CHIAMATE_ENTRATA)));

                switch (scelta) {
                    case HomeActivity.CHIAMATE_ENTRATA:
                    case HomeActivity.CHIAMATE_USCITA: {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                            HomeActivity.ratings = HomeActivity.getCallLog(context);
                        }
                        break;
                    }
                    case HomeActivity.CONTATTI: {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                            HomeActivity.ratings = HomeActivity.getRatingContacts();
                        }
                        break;
                    }
                    case HomeActivity.MIEI_FEEDBACK: {
                        HomeActivity.ratings = new ArrayList<>(HomeActivity.ratingsOnDb.values());
                        break;
                    }
                    default:
                        break;
                }
                HomeActivity.showRatings(context);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Main", "Failed to read value.", error.toException());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void resetPreferences(SharedPreferences.Editor editor, boolean all){
        if(all){
            editor.putString("email", "");
            editor.putString("password", "");
            editor.putString("piva", "");
            editor.putString("uid", "");
        }

        editor.putString("notificationPreference", String.valueOf(R.id.notification));
        //editor.putString("filter", String.valueOf(HomeActivity.NO_FILTER));
        editor.putString("scelta", String.valueOf(HomeActivity.CHIAMATE_ENTRATA));    //chiamate in entrata
        editor.apply();
    }
}
