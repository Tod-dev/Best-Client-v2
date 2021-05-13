package com.example.progettobiancotodaro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.progettobiancotodaro.httpRequest.HttpManager;
import com.example.progettobiancotodaro.httpRequest.RequestPackage;
import com.example.progettobiancotodaro.script.BigToAvg;
import com.example.progettobiancotodaro.ui.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;


@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

   // GoogleSignInClient mGoogleSignInClient;
    Button ratingButton;
    Button settingsbutton;
    String[] Permissions = new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_PHONE_NUMBERS,Manifest.permission.READ_CALL_LOG};
    SharedPreferences sp;
    final String uri = "http://worldtimeapi.org/api/timezone/Europe/Rome";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            //Uri personPhoto = acct.getPhotoUrl();
            Toast.makeText(this,personName+" "+personEmail+" "+personId, Toast.LENGTH_SHORT).show();
        }
        logoutButton = findViewById(R.id.SignOutButton);
        logoutButton.setOnClickListener(v -> {
            // ...
            if (v.getId() == R.id.SignOutButton) {
                signOut();
                // ...
            }
        });*/
        //SCRIPT
        requestData();

        //ACtivity
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setTitle(R.string.app_name);

        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)+
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, Permissions, 1);
        }

        /*if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_NUMBERS},2);
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG},3);
        }*/

        ratingButton = findViewById(R.id.AddRatingButton);
        ratingButton.setOnClickListener(v -> {
            if((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED)){
                //se ha i permessi avvia add rating
                Intent intent = new Intent(MainActivity.this, AddRating.class);
                startActivity(intent);
            }
            else Toast.makeText(this,"This app couldn't read phone numbers and call log, please allow in settings", Toast.LENGTH_LONG).show();

        });

        settingsbutton = findViewById(R.id.settingsButton);
        settingsbutton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.home_menu, menu);
        menu.findItem(R.id.logout).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart(){
        super.onStart();
        sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String email = sp.getString("email", "");
        String password = sp.getString("password", "");
        //String uid = sp.getString("uid", "");
        //Toast.makeText(this, email+" "+password+" "+uid, Toast.LENGTH_LONG).show();

        if(email.equals("") || password.equals("")){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        //Toast.makeText(this,"sign in", Toast.LENGTH_SHORT).show();
        /*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if(account == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }*/

    }
    /*
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.logout){
            sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

            @SuppressLint("CommitPrefEdits")
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("email", "");
            editor.putString("password", "");
            editor.putString("uid", "");
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void requestData() {
        Log.d("REQUEST DATA:","SONO IN REQUEST DATA");
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(uri);

        Downloader downloader = new Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }

    private static class Downloader extends AsyncTask<RequestPackage, String, String> {
        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        //The String that is returned in the doInBackground() method is sent to the
        // onPostExecute() method below. The String should contain JSON data.
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.d("DOWNLOADER:","SONO IN DOWNLOADER : "+result);
                //We need to convert the string in result to a JSONObject
                if(result == null) return;
                JSONObject jsonObject = new JSONObject(result);
                Log.d("JSON:",jsonObject.toString());
                //The “ask” value below is a field in the JSON Object that was
                //retrieved from the BitcoinAverage API. It contains the current
                //bitcoin price
                long unixTime = jsonObject.getLong("unixtime");
                unixTime *= 1000; //timestamp in ms
                Log.d("Data:",""+unixTime);
                BigToAvg.update(unixTime);

                //Now we can use the value in the mPriceTextView
                //mPriceTextView.setText(price);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
/*tv = (TextView) findViewById(R.id.informazioni);


        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] {"number", "date"}, null, null, "date DESC");

        StringBuffer stringBuffer = new StringBuffer();

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        int offset = 0;
        final int limit = 10;

        int i = 0;
        while(i < offset && c.moveToNext()){
            i++;
        }

        while(c.moveToNext() && i < limit){
            String number = c.getString(colNumber);
            Date date = new Date(Long.valueOf(c.getString(colDate)));
            stringBuffer.append("\nNumber: "+number);
            stringBuffer.append("\nDate: "+date);
            i++;
        }
        c.close();
        tv.setText(stringBuffer);*/



/* // Read from the database
        ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                    /*String value = dataSnapshot.getValue(String.class);
                    Log.d("Main", "Value is: " + value);
                oldfirebaseRating = dataSnapshot.getValue(RatingOnDB.class);
        Log.w("OLD FIREBASE RATING", "");
                    /*for(DataSnapshot d : dataSnapshot.getChildren()){
                        r = (RatingOnDB) d.getValue(RatingOnDB.class);
                    }

                    Toast t = Toast.makeText(MainActivity.this, r.getPhoneNumber()+" "+r.getRatings(), Toast.LENGTH_LONG);
                    t.show();
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Main", "Failed to read value.", error.toException());
            }
        });
*/