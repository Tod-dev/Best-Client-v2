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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import it.bestclient.android.DB.DBhelper;
import it.bestclient.android.RatingModel.RatingBigOnDB;
import it.bestclient.android.RatingModel.RatingLocal;
import it.bestclient.android.components.Contact;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Utils {
    /**
     * Classe che contiene metodi statici per manipolare i dati nell'app
     */

    public static void toastMessage(String message, Context k){
        Toast.makeText(k,message, Toast.LENGTH_SHORT).show();
    }

    /*POST NEW RATING TO RATINGBIG TABLE ON FIREBASE (REST)*/
    public static void updateDB(RatingBigOnDB r){
        Log.d("ratingonDB:", "Sto USANDO IL DB: scrivo: "+ r.toString() ); //ratingBig
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showDialog(Context context, int type, RatingLocal r, DBhelper myDBhelper, String uid){
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
                            UpdateData(context, r, false, myDBhelper, uid);
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
        else if (type == 2) { //Dialog dai un voto / cancella un elemento
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

            builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        float nuovoRating =  ratingbar.getRating();

                        if(!Objects.requireNonNull(comment.getEditText()).getText().toString().equals("")){
                            r.setCommento(removeQuotes(comment.getEditText().getText().toString()));
                        }
                        if(nuovoRating != 0){
                            //se ho inserito un rating modifico il db firebase
                            r.setVoto(nuovoRating);
                            try {
                                UpdateData(context, r, true, myDBhelper, uid);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            if(!comment.getEditText().getText().toString().equals("")){
                                try {
                                    UpdateData(context, r, false, myDBhelper, uid);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        HomeActivity.showRatings(context);
                        //refreshView(r, listView);

                        //ratings.get(i1).setRating(rating.getRating());
                        //Toast.makeText(AddRating.this,Float.toString(ratingbar.getRating()),Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
            deleteButton.setOnClickListener(v -> {
                dialog.dismiss();
                showDialog(context, 1,r, myDBhelper, uid);
            });
            commentButton.setOnClickListener(v -> {
                comment.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.INVISIBLE);
            });

        }
        else if(type == 3){
            builder.setTitle(R.string.dialogMessage);
            View viewDialog = inflater.inflate(R.layout.rating_stars, null);
            TextInputLayout comment = viewDialog.findViewById(R.id.comment);
            TextInputEditText commentText = viewDialog.findViewById(R.id.commentText);
            RatingBar ratingbar = viewDialog.findViewById(R.id.ratingStars);
            ImageView deleteButton = viewDialog.findViewById(R.id.delete);
            deleteButton.setVisibility(View.INVISIBLE);
            ImageView commentButton = viewDialog.findViewById(R.id.commentLogo);

            builder.setView(viewDialog).setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                float rating = ratingbar.getRating(); //valutazione inserita
                String commento = commentText.getText().toString();  //commento inserito
                r.setVoto(rating);
                r.setCommento(removeQuotes(commento));

                try {
                    UpdateData(context, r, true, myDBhelper, uid);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                ContactsActivity.showRatings(context);

                dialog.dismiss();
            }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            commentButton.setOnClickListener(v -> {
                comment.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.INVISIBLE);
            });
        }
    }

    public static void UpdateData(Context context, RatingLocal r, boolean db, DBhelper myDBhelper, String uid) throws ParseException {
        /* AGGIORNA I DATI SUL DB SQLITE E SU FIREBASE */
        Date date = new Date();
        RatingBigOnDB remoteRating = new RatingBigOnDB(uid,date,r.getVoto(),r.getNumero(),r.getCommento());

        if(db)
            updateDB(remoteRating);

        /*if(r.getVoto() > 0){
            r.setVoto(-2);
        }*/

        int ret = myDBhelper.updateRating(r);
        if(ret == -1){
            boolean insertData = myDBhelper.addData(r.getNumero(),r.getDate(),r.getVoto(), r.getCommento());

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

}
