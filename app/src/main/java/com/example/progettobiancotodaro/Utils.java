package com.example.progettobiancotodaro;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.progettobiancotodaro.RatingModel.RatingBigOnDB;
import com.example.progettobiancotodaro.RatingModel.RatingLocal;
import com.example.progettobiancotodaro.components.Contact;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utils {
    /**
     * Classe che contiene metodi statici per manipolare i dati nell'app
     */

    public static void toastMessage(String message, Context k){
        Toast.makeText(k,message, Toast.LENGTH_SHORT).show();
    }

    /*POST NEW RATING TO RATINGBIG TABLE ON FIREBASE (REST)*/
    public static void updateDB(RatingBigOnDB r){
        Log.d("ratingonDB:", "Sto USANDO IL DB"); //ratingBig
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("ratingBig");
        mDatabase.push().setValue(r);
    }


    public static List<Contact> fetchContacts(ContentResolver contentResolver, Context k){
        List<Contact> contacts = new ArrayList<>();
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
                                contact.setPhone(filterOnlyDigits(phoneinContact));
                            }
                            if (phoneCursor != null) phoneCursor.close();
                        }
                        //contact.image = ContactPhoto(contact_id);
                        contacts.add(contact);
                    }
                    //ho tutti i contatti in contacts
                }
            } catch (Exception ignored) {
            }

        }
        return contacts;
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
}
