package it.bestclient.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.bestclient.android.RatingModel.Rating;
import it.bestclient.android.RatingModel.RatingCallLog;
import it.bestclient.android.components.Contact;
import it.bestclient.android.components.RowAdapter;

import static it.bestclient.android.Utils.fetchContacts;
import static it.bestclient.android.Utils.toastMessage;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    // GoogleSignInClient mGoogleSignInClient;
    public static SharedPreferences sp;
    SharedPreferences.Editor editor;
    @SuppressLint("StaticFieldLeak")
    //static ListView listView;

    public static ActionBar actionBar;

    public static final int CHIAMATE_ENTRATA = 0;
    public static final int CHIAMATE_USCITA = 1;
    //public static final int ULTIME_24H = 2;
    public static final int MIEI_FEEDBACK = 3;
    //public static final int NO_FILTER = 4;
    public static final int CONTATTI = 5;

    public static RecyclerView recyclerView;
    Context context;
    public static String[] phoneNumbers;
    public static int[] logos;
    //public static String[] names;
    public static double[] ratingDouble;
    public static double[] ratingAVGDouble;
    public static int[] nValutazioni;
    @SuppressLint("StaticFieldLeak")
    public static RowAdapter arrayAdapter;
    public static List<Contact> contacts = new ArrayList<>();
    public static Set<RatingCallLog> callLogNumbers = new HashSet<>();
    public static Map<String, String> contactMap = new HashMap<>();
    public static Map<String, Rating> ratingsOnDb = new HashMap<>(); //mappa di rating presenti sul db (presi con un'unica richiesta)
    //static final int MAX_ITEMS = 100;
    public static List<Rating> ratings = new ArrayList<>();
    public static List<Rating> lastRatings;
    private ProgressBar progressBar;
    //private final Handler mainHandler = new Handler();
    SwipeRefreshLayout swipeRefreshLayout;
    boolean contactsActive = false;
    public static boolean checkDataOnDB = false;


    @Override
    protected void onStart() {
        super.onStart();

        String email = sp.getString("email", "");
        String password = sp.getString("password", "");

        if (email.equals("") || password.equals("")) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.to_left_in, R.anim.to_right_out);
        }

    }

    @SuppressLint({"NonConstantResourceId", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*ActionBar set title*/
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_account_circle_24_white);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        context = this;
        recyclerView = findViewById(R.id.list);
        sp = this.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        editor = sp.edit();
        progressBar = findViewById(R.id.progressBar);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            int scelta = Integer.parseInt(sp.getString("scelta", String.valueOf(CHIAMATE_ENTRATA)));
            switch (scelta) {
                case CHIAMATE_ENTRATA:
                case CHIAMATE_USCITA: {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                        startAsyncTask(1);
                    }
                    break;
                }
                case CONTATTI: {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        startAsyncTask(2);
                    }
                    break;
                }
                case MIEI_FEEDBACK: {
                    startAsyncTask(3);
                    break;
                }
                default:
                    break;
            }
            swipeRefreshLayout.setRefreshing(false);
        });

        //prendo la lista dei contatti una sola volta
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                contacts.size() == 0) {
            startAsyncTask(0);
        }

        //prendo le valutazioni dal database UNA VOLTA SOLA
        if (!checkDataOnDB) {
            startAsyncTask(4);
        } else {

            int scelta = Integer.parseInt(sp.getString("scelta", String.valueOf(CHIAMATE_ENTRATA)));

            switch (scelta) {
                case CHIAMATE_ENTRATA:
                case CHIAMATE_USCITA: {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                        startAsyncTask(1);
                    }
                    break;
                }
                case CONTATTI: {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        startAsyncTask(2);
                    }
                    break;
                }
                case MIEI_FEEDBACK: {
                    startAsyncTask(3);
                    break;
                }
                default:
                    break;
            }
        }

    }

    public void startAsyncTask(int type) {
        new LoadRatingsAsync(this).execute(type);
    }

    public static void showRatings(Context context) {
        if (ratings != null && checkDataOnDB) {
            Log.d(TAG, "listRating:  " + "HELLO");
            ratingToString(context);
            /* INSERT ALL THE RATINGS IN THE LISTVIEW */
            arrayAdapter = new RowAdapter((Activity) context, context, logos, phoneNumbers, ratingDouble, ratingAVGDouble, nValutazioni);
            recyclerView.setAdapter(arrayAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            TextView t = ((Activity) context).findViewById(R.id.noResults);
            if (ratings.isEmpty()) {
                t.setVisibility(View.VISIBLE);
            } else {
                t.setVisibility(View.GONE);
            }
        }
    }

    public static void showRatings(Context context, List<Rating> listRating) {
        if (listRating != null) {
            Log.d(TAG, "listRating:  " + listRating.toString());
            ratingToString(listRating);
            /* INSERT ALL THE RATINGS IN THE LISTVIEW */
            arrayAdapter = new RowAdapter((Activity) context, context, logos, phoneNumbers, ratingDouble, ratingAVGDouble, nValutazioni);
            recyclerView.setAdapter(arrayAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            ratings = listRating;
            TextView t = ((Activity) context).findViewById(R.id.noResults);
            if (ratings.isEmpty()) {
                t.setVisibility(View.VISIBLE);
            } else {
                t.setVisibility(View.GONE);
            }
        }

    }

    private Drawable getDrawableResource(int resID) {
        return ContextCompat.getDrawable(this, resID);
    }


    /*Menu creation -> add button refresh*/
    @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables"})
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        menu.findItem(R.id.searchBtn).setVisible(true);

        MenuItem search = menu.findItem(R.id.searchBtn);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setBackgroundColor(R.color.white);
        searchView.setBackground(getDrawableResource(R.drawable.sfondo_trasparente));
        searchView.setQueryHint("Cerca...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                arrayAdapter.filter(newText);
                return true;

            }

        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(HomeActivity.this, UserActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.to_left_in, R.anim.to_right_out);
            return true;
        }

        if (item.getItemId() == R.id.uscita) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                editor.putString("scelta", String.valueOf(CHIAMATE_USCITA));
                //editor.putString("filter", String.valueOf(NO_FILTER));
                editor.apply();
                contactsActive = false;

                startAsyncTask(1);
                return true;
            } else {
                Toast.makeText(this, "L'app non ha accesso al registro delle chiamate, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (item.getItemId() == R.id.entrata) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                editor.putString("scelta", String.valueOf(CHIAMATE_ENTRATA));
                //editor.putString("filter", String.valueOf(NO_FILTER));
                editor.apply();
                contactsActive = false;

                startAsyncTask(1);
                return true;
            } else {
                Toast.makeText(this, "L'app non ha accesso al registro delle chiamate, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (item.getItemId() == R.id.contatti) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                contactsActive = true;
                editor.putString("scelta", String.valueOf(CONTATTI));
                editor.apply();

                startAsyncTask(2);
                return true;
            } else {
                Toast.makeText(this, "L'app non ha accesso ai contatti, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
                return false;
            }

        }

        /*if (item.getItemId() == R.id.ultime24h) {
            int scelta = Integer.parseInt(sp.getString("scelta", String.valueOf(CHIAMATE_ENTRATA)));
            if(scelta == CHIAMATE_ENTRATA || scelta == CHIAMATE_USCITA){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                    editor.putString("filter", String.valueOf(ULTIME_24H));
                    editor.apply();
                    contactsActive = false;

                    startAsyncTask(1);
                    return true;
                }
                else{
                    Toast.makeText(this, "L'app non ha accesso al registro delle chiamate, abilitalo dalle impostazioni", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            else return false;
        }*/

        if (item.getItemId() == R.id.mieiFeedback) {
            editor.putString("scelta", String.valueOf(MIEI_FEEDBACK));
            editor.apply();
            contactsActive = false;

            startAsyncTask(3);
            return true;
        }

        return false;
    }


    public static List<Rating> getCallLog(Context context) {
        int scelta = Integer.parseInt(sp.getString("scelta", String.valueOf(CHIAMATE_ENTRATA)));
        /*GET CURSOR FOR THE CALLS LOG*/
        Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{"number", "date", "type"}, null, null, "date DESC");

        int colNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
        int colDate = c.getColumnIndex(CallLog.Calls.DATE);

        //int filter = Integer.parseInt(sp.getString("filter", String.valueOf(HomeActivity.NO_FILTER)));

        List<Rating> ratingsRet = new ArrayList<>();
        List<RatingCallLog> ratingCallLogs = new ArrayList<>();

        //Date curDate = Calendar.getInstance().getTime();
        /*READ CALLS LOG, LINE BY LINE*/
        //int count = 0;
        while (c.moveToNext()) {
            //Log.d("i, array:  ", ""+i + Arrays.toString(ratings.toArray()));
            //count++;
            //boolean skip = false;

            String type = c.getString(c.getColumnIndex(CallLog.Calls.TYPE));
            int typeNumber = Integer.parseInt(type);

            /*NEW RATING*/
            String number = c.getString(colNumber);
            Date date = new Date(Long.parseLong(c.getString(colDate)));

            RatingCallLog check = new RatingCallLog(number, date);

            /*aggiungo indistintamente il numero al set*/
            callLogNumbers.add(check);

            if ((scelta == CHIAMATE_ENTRATA && typeNumber == CallLog.Calls.OUTGOING_TYPE) ||
                    (scelta == CHIAMATE_USCITA && typeNumber != CallLog.Calls.OUTGOING_TYPE))
                continue;


            //long diffInMillies = Math.abs(date.getTime() - curDate.getTime());
            //long diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            /*FILTER in case settings say lastN*/
            /*if (filter == ULTIME_24H) {
                if (diff > 24) skip = true;
            }*/

            /*IF THE RATING DOES NOT MATCH SETTINGS DATE*/
            //if(skip) continue;

            /*IF THE RATING IS NOT THE FIRST ONE OF ITS TYPE(GROUP): (DAY,PHONE) -> skip*/
            boolean checkIfExist = false;
            for (RatingCallLog r : ratingCallLogs) {
                boolean res = r.group_by(check);
                //Log.d("CHECK_CONFRONTO: ", ""+res);
                if (res) {
                    checkIfExist = true;
                    break;
                }
            }

            /*ADD A NEW RATING TO THE LIST*/
            if (!checkIfExist) {
                ratingCallLogs.add(check);
                //.d(TAG, "getCallLogCHECK:  number: "+number+" -> contacts: " + contactMap.toString());

                Rating r;

                //se il numero è nella lista contatti salvo il nome
                if (contactMap.containsKey(check.getNumero()))
                    r = new Rating(check.getNumero(), contactMap.get(check.getNumero()));
                else r = new Rating(check.getNumero());

                //se il numero è stato valutato imposto il suo voto
                if (ratingsOnDb.containsKey(r.getNumero()))
                    r.setVoto(ratingsOnDb.get(r.getNumero()).getVoto());

                ratingsRet.add(r);
            }
        }
        c.close();

        return ratingsRet;
    }

    public static List<Rating> getRatingContacts() {
        List<Rating> ratingsRet = new ArrayList<>();
        //int count = 0;
        for (Contact c : contacts) {

            if (ratingsOnDb.containsKey(c.getPhone())) {
                ratingsRet.add(ratingsOnDb.get(c.getPhone()));
            } else ratingsRet.add(new Rating(c.getPhone(), c.getName()));
        }

        return ratingsRet;
    }

    /*public List<Rating> getMyFeedbacks(){
        return new ArrayList<>(ratingsOnDb.values());
    }*/


    public static void ratingToString(Context context) {
        logos = new int[ratings.size()];
        Arrays.fill(logos, R.drawable.phone);
        phoneNumbers = new String[ratings.size()];
        ratingDouble = new double[ratings.size()];
        ratingAVGDouble = new double[ratings.size()];
        nValutazioni = new int[ratings.size()];

        int i = 0;
        for (Rating r : ratings) {
            phoneNumbers[i] = r.getNumero();

            ratingDouble[i] = r.getVoto();

            Utils.getRatingAVG(context, r, i); //prendo il rating AVG del numero corrente
            i++;
        }

    }

    public static void ratingToString(List<Rating> ratingList) {
        logos = new int[ratingList.size()];
        Arrays.fill(logos, R.drawable.phone);
        phoneNumbers = new String[ratingList.size()];
        ratingDouble = new double[ratingList.size()];
        ratingAVGDouble = new double[ratingList.size()];
        nValutazioni = new int[ratingList.size()];

        int i = 0;
        for (Rating r : ratingList) {
            phoneNumbers[i] = r.getNumero();

            ratingDouble[i] = r.getVoto();

            ratingAVGDouble[i] = r.getVoto_medio();

            nValutazioni[i] = r.getnValutazioni();

            if (r.getVoto() > 0 || r.getVoto_medio() > 0) {
                logos[i] = R.drawable.logo_red;
            }
            if (r.isAdder()) {
                logos[i] = R.drawable.adder;
            }
            i++;
        }

    }


    @SuppressLint("StaticFieldLeak")
    private class LoadRatingsAsync extends AsyncTask<Integer, Void, Void> {
        private final WeakReference<HomeActivity> activityWeakReference;
        private int currentTask;
        //final ProgressDialog dialog = new ProgressDialog(HomeActivity.this);

        LoadRatingsAsync(HomeActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /*dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Carico...");
            dialog.show();*/

            HomeActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.progressBar.setVisibility(View.VISIBLE);
            activity.swipeRefreshLayout.setVisibility(View.GONE);
            //Toast.makeText(activity, "START", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected Void doInBackground(Integer... val) {
            //showRatings(activityWeakReference.get().context);
            int type = val[0];
            this.currentTask = type;
            runOnUiThread(() -> {
                if (type == 0) {
                    ContentResolver contentResolver = activityWeakReference.get().getContentResolver();
                    contacts = fetchContacts(contentResolver, activityWeakReference.get().context);
                } else if (type == 1) {
                    activityWeakReference.get();
                    int scelta = Integer.parseInt(sp.getString("scelta", String.valueOf(CHIAMATE_ENTRATA)));
                    if (scelta == CHIAMATE_ENTRATA) actionBar.setTitle(R.string.entrata);
                    else actionBar.setTitle(R.string.uscita);
                    ratings = getCallLog(activityWeakReference.get().context);
                    showRatings(activityWeakReference.get().context);
                    lastRatings = ratings;
                } else if (type == 2) {
                    actionBar.setTitle(R.string.contatti);
                    ratings = getRatingContacts();
                    showRatings(activityWeakReference.get().context);
                    lastRatings = ratings;
                } else if (type == 3) {
                    actionBar.setTitle(R.string.mieiFeedback);
                    ratings = new ArrayList<>(ratingsOnDb.values());
                    //ratings = (List<Rating>) ratingsOnDb.values();
                    Collections.sort(ratings);
                    //Collections.reverse(ratings);
                    showRatings(activityWeakReference.get().context);
                    lastRatings = ratings;
                } else if (type == 4) {
                    getDataFromDB(activityWeakReference.get().context);
                    lastRatings = ratings;
                }
            });
            //Log.d(TAG,"LAST RATINGS: "+lastRatings.toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            //dialog.dismiss();

            HomeActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            //Toast.makeText(activity, "ok", Toast.LENGTH_SHORT).show();
            //activity.progressBar.setProgress(0);
            if (currentTask != 4 && currentTask != 0) { /*IN questi casi voglio che il caricamento rimanga visibile*/
                activity.progressBar.setVisibility(View.GONE);
                activity.swipeRefreshLayout.setVisibility(View.VISIBLE);
            }
            //Toast.makeText(activity, "END", Toast.LENGTH_SHORT).show();
        }
    }

    //Funzione che scarica la lista di valutazioni dell'utente collegato e le inserisce in ratingsOnDb
    public void getDataFromDB(Context context) {

        String uid = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE).getString("uid", "");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("Users").child(uid).child("Valutazioni");

        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Rating r = d.getValue(Rating.class);
                    r.setNumero(d.getKey());

                    if (HomeActivity.contactMap.containsKey(r.getNumero()))
                        r.setNome(HomeActivity.contactMap.get(r.getNumero()));
                    else r.setNome("");

                    HomeActivity.ratingsOnDb.put(r.getNumero(), r);

                }
                HomeActivity.checkDataOnDB = true;
                int scelta = Integer.parseInt(HomeActivity.sp.getString("scelta", String.valueOf(HomeActivity.CHIAMATE_ENTRATA)));

                switch (scelta) {
                    case HomeActivity.CHIAMATE_ENTRATA:
                    case HomeActivity.CHIAMATE_USCITA:
                        startAsyncTask(1);
                        break;
                    case HomeActivity.CONTATTI: {
                        startAsyncTask(2);
                        break;
                    }
                    case HomeActivity.MIEI_FEEDBACK: {
                        startAsyncTask(3);
                        break;
                    }
                    default:
                        break;
                }
                //lastRatings=HomeActivity.ratings;
                //Log.d("Utils","LAST RATINGS: "+lastRatings.toString());
                //HomeActivity.showRatings(context);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Main", "Failed to read value.", error.toException());
            }
        });

    }
}
