package com.example.progettobiancotodaro;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.example.progettobiancotodaro.DB.DBhelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.StringTokenizer;

public class IncomingReceiver extends BroadcastReceiver {
    //final String OUR_ACTION = "android.intent.action.PHONE_STATE";
    Context context;
    DBhelper myDBhelper;
    RatingOnDB curRating;
   // private NotificationManagerCompat notificationManager;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            this.context = context;
            myDBhelper = new DBhelper(this.context);
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            CallListener cl = new CallListener();
            tm.listen(cl, PhoneStateListener.LISTEN_CALL_STATE);

        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Log.d("CALL: ", "CALL ENDED");
        }
    }

    private class CallListener extends PhoneStateListener{

        public void onCallStateChanged(int state, String incomingNumber){

            if (incomingNumber.length() > 0 && state == TelephonyManager.CALL_STATE_RINGING) {

                /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String notificationPreference = preferences.getString("Notification", "Toast Message");

                String message;*/
                String onlyNumber = incomingNumber.substring(incomingNumber.length() - 10);

                getRatingFromNumber(onlyNumber);

                //float rating = myDBhelper.getRating(onlyNumber);
                /*if(curRating.getRatings().equals("notRated")){
                    message = "Number not rated";
                }
                else{
                    float rating = CalculateAvgRating(curRating);

                    if(rating == 0){
                        message = onlyNumber+": 0 stars!";
                    }
                    else{
                        int roundRating = Math.round(rating);
                        StringBuilder feedBack = new StringBuilder();

                        for(int i = 0; i < roundRating; i++){
                            feedBack.append("⭐");
                        }
                        message = onlyNumber+ " "+feedBack.toString();

                    }

                }

                if(notificationPreference.equals("toast_message")){
                    makeToast(message);
                }
                else if(notificationPreference.equals("notification")){
                    makeNotification(message);
                }*/


            }


        }

        public void makeToast(String message){
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }

        public void makeNotification(String message){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel("Rating Notification", "Rating Notification", NotificationManager.IMPORTANCE_HIGH);
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Rating Notification");
            builder.setContentTitle("Rating Manager");
            builder.setContentText(message);
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
            builder.setSmallIcon(R.drawable.ic_baseline_star);
            builder.setAutoCancel(true);

            NotificationManagerCompat notificationCompat = NotificationManagerCompat.from(context);
            notificationCompat.notify(1, builder.build());
        }

        //Selects the object from firebase db using phone number and copies it into curRating object
        public void getRatingFromNumber(String number){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ratingsRef = database.getReference("ratings").child(number);


            ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    String notificationPreference = preferences.getString("Notification", "Toast Message");
                    String message;
                    if(dataSnapshot.getValue() == null){
                        message = "Number not rated";
                        //curRating = new RatingOnDB(number,"notRated");
                        //Toast.makeText(AddRating.this, "null", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        curRating = dataSnapshot.getValue(RatingOnDB.class);
                        float rating = CalculateAvgRating(curRating);

                        if(rating == 0){
                            message = number+": 0 stars!";
                        }
                        else{
                            int roundRating = Math.round(rating);
                            StringBuilder feedBack = new StringBuilder();

                            for(int i = 0; i < roundRating; i++){
                                feedBack.append("⭐");
                            }
                            message = number+ " "+feedBack.toString();

                        }
                        //Toast.makeText(AddRating.this, r.toString(), Toast.LENGTH_SHORT).show();
                    }

                    if(notificationPreference.equals("toast_message")){
                        makeToast(message);
                    }
                    else if(notificationPreference.equals("notification")){
                        makeNotification(message);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.w("Main", "Failed to read value.", error.toException());
                }
            });
        }

        //Calculates average rating of a RatingOnDB object
        public float CalculateAvgRating(RatingOnDB ratingOnDB){
            StringTokenizer stringTokenizer = new StringTokenizer(ratingOnDB.getRatings(), ";");
            float avg = 0;
            float i = 0;
            while(stringTokenizer.hasMoreTokens()){
                avg += Float.parseFloat(stringTokenizer.nextToken());
                i++;
            }

            if(i == 0) return 0;

            return avg / i;
        }

    }

}