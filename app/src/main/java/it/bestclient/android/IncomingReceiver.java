package it.bestclient.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import it.bestclient.android.DB.DBhelper;
import it.bestclient.android.RatingModel.RatingAVGOnDB;
import it.bestclient.android.components.Contact;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static it.bestclient.android.Utils.filterOnlyDigits;

public class IncomingReceiver extends BroadcastReceiver {
    //final String OUR_ACTION = "android.intent.action.PHONE_STATE";
    Context context;
    DBhelper myDBhelper;
    RatingAVGOnDB curRating;
    SharedPreferences sp;
   // private NotificationManagerCompat notificationManager;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                /*THE PHONE IS RINGING*/
                this.context = context;
                myDBhelper = new DBhelper(this.context);
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                CallListener cl = new CallListener();
                tm.listen(cl, PhoneStateListener.LISTEN_CALL_STATE);

            } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d("CALL: ", "CALL ENDED");
                //Refresh view ?
            }
        }
        else Toast.makeText(context,"This app couldn't read phone state, please allow in settings", Toast.LENGTH_LONG).show();

    }

    private class CallListener extends PhoneStateListener{

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onCallStateChanged(int state, String incomingNumber){

            if (incomingNumber.length() > 0 && state == TelephonyManager.CALL_STATE_RINGING) {
                sp = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
                String email = sp.getString("email", "");
                String password = sp.getString("password", "");

                if(email.equals("") || password.equals("")){
                    return;
                }

                /* ESTRACT THE NUMBER OF THE CALLER */
                String onlyNumber=incomingNumber;
                /*IF THE number w the PREFIX >= 10 -> we read only the LAST 10 character */
                if(incomingNumber.length() >= 10)
                    onlyNumber = incomingNumber.substring(incomingNumber.length() - 10);

                getRatingFromNumber(onlyNumber);
            }
        }

        /* DISPLAY OF TOAST */
        public void makeToast(String message){
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }

        /* DISPLAY OF NOTIFICATION */
        public void makeNotification(String message){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel("Rating Notification", "Rating Notification", NotificationManager.IMPORTANCE_HIGH);
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Rating Notification");
            builder.setContentTitle("Rating Manager");
            builder.setContentText(message);
            builder.setSmallIcon(R.drawable.ic_baseline_star_24);
            builder.setColorized(true);
            builder.setPriority(Notification.PRIORITY_MAX);

            NotificationManagerCompat notificationCompat = NotificationManagerCompat.from(context);
            notificationCompat.notify(1, builder.build());
        }

        /* DISPLAY OF POPUP */
        public void makePopup(String title, String dialogTxt){
            Intent intent = new Intent(context, PopUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("title", title);
            intent.putExtra("text", dialogTxt);
            context.startActivity(intent);
        }

        /*Selects the object from firebase db using phone number and copies it into curRating object -> than send a notification (setting switch the notification type) */
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void getRatingFromNumber(String number){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ratingsRef = database.getReference("ratingAVG").child(number);

            //CONTROLLO SE IL NUMERO è in rubrica -> lo sostituisco

            for(Contact c : HomeActivity.contacts){
                if(filterOnlyDigits(number).equals(filterOnlyDigits(c.getPhone()))){
                    //ho trovato il contatto
                    number = c.getName();
                    break;
                }
            }

            String finalNumber = number;
            ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int preference = Integer.parseInt(sp.getString("notificationPreference", String.valueOf(R.id.notification)));
                    String message;
                    String dialogTxt;
                    if(dataSnapshot.getValue() == null){
                        message = finalNumber + ": Number not rated";
                        dialogTxt=message;
                        //curRating = new RatingOnDB(number,"notRated");
                        //Toast.makeText(AddRating.this, "null", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        double val = dataSnapshot.getValue(Double.class);
                        curRating = new RatingAVGOnDB(val);
                        double rating = curRating.getVoto();
                        /*
                        if (curRating != null) {
                            rating = CalculateAvgRating(curRating);
                        }
                        */
                        if(rating == 0){
                            message = finalNumber +": 0 stars!";
                            dialogTxt="0 stars!";
                        }
                        else{
                            int roundRating = (int) Math.floor(rating);
                            StringBuilder feedBack = new StringBuilder();

                            double resto = rating-Math.floor(rating);
                            for(int i = 0; i < roundRating; i++){
                                feedBack.append("⭐");
                            }
                            if(resto > 0){
                                feedBack.append("☆");
                            }

                            message = finalNumber + " "+feedBack.toString();
                            dialogTxt=feedBack.toString();

                        }
                        //Toast.makeText(AddRating.this, r.toString(), Toast.LENGTH_SHORT).show();
                    }

                    switch (preference) {
                        case R.id.toast:
                            makeToast(message);
                            break;
                        case R.id.notification:
                            makeNotification(message);
                            break;
                        case R.id.popup:
                            makePopup(finalNumber, dialogTxt);
                            break;
                        default:
                            makeNotification(message);
                            break;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.w("Main", "Failed to read value.", error.toException());
                }
            });
        }


        /*
        //Calculates average rating of a RatingOnDB object
        public float CalculateAvgRating(RatingAVGOnDB ratingAVGOnDB){
            StringTokenizer stringTokenizer = new StringTokenizer(ratingAVGOnDB.getRating(), ";");
            float avg = 0;
            float i = 0;
            while(stringTokenizer.hasMoreTokens()){
                avg += Float.parseFloat(stringTokenizer.nextToken());
                i++;
            }

            if(i == 0) return 0;

            return avg / i;
        }
        */
    }

}