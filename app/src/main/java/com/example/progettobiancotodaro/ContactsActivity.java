package com.example.progettobiancotodaro;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.progettobiancotodaro.RatingModel.RatingBigOnDB;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    public static List<Contact> contacts = null;
    SharedPreferences sp;
    String[] name;
    String[] phoneNumber;
    ListView listView;
    String uid;

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

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
            this.uid = sp.getString("uid", "");

            listView = findViewById(R.id.listcontacts);

            //prendo tutti i contatti dalla rubrica
            contacts = fetchContacts();
            name = new String[contacts.size()];
            phoneNumber = new String[contacts.size()];

            //salvo i nomi e i numeri di telefono nei vettori
            int index = 0;
            for (Contact c : contacts) {
                name[index] = c.name;
                phoneNumber[index] = c.phone;
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
                    Date date = Calendar.getInstance().getTime();   //data corrente
                    float rating = ratingbar.getRating(); //valutazione inserita
                    String commento = commentText.getText().toString();  //commento inserito
                    String number = phoneNumber[position];

                    RatingBigOnDB remoteRating = new RatingBigOnDB(uid, date, rating, number, commento);

                    //salvo i dati su firebase
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("ratingBig");
                    mDatabase.push().setValue(remoteRating);

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
        else Toast.makeText(this, "This app hasn't access to phone numbers, allow in settings", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Contact> fetchContacts(){
        List<Contact> contacts = new ArrayList<>();
        /*controllo se sono in rubrica*/
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            //solo se ho i permessi di accesso alla rubrica
            ContentResolver contentResolver = getContentResolver();
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
                        contact.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
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
                                contact.phone = HomeActivity.filterOnlyDigits(phoneinContact);
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

    public class Contact {
        String name = "";
        String phone = "";
        //Bitmap image = null;


        @Override
        public String toString() {
            return "Contact{" +
                    "name='" + name + '\'' +
                    ", phone='" + phone + '\'' +
                    '}';
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