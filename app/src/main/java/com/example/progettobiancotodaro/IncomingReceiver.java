package com.example.progettobiancotodaro;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.progettobiancotodaro.DB.DBhelper;

public class IncomingReceiver extends BroadcastReceiver {
    //final String OUR_ACTION = "android.intent.action.PHONE_STATE";
    Context context;
    DBhelper myDBhelper;
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

                String message;
                String onlyNumber = incomingNumber.substring(incomingNumber.length() - 10);

                float rating = myDBhelper.getRating(onlyNumber);
                if(rating == -1){
                    message = "Number not rated";
                }
                else{
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

                makeToast(message);
                makeNotification(message);
            }


        }

        public void makeToast(String message){
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }

        public void makeNotification(String message){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel("Rating Notification", "Rating Notification", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Rating Notification");
            builder.setContentTitle("Rating Manager");
            builder.setContentText(message);
            builder.setSmallIcon(R.drawable.ic_baseline_star);
            builder.setAutoCancel(true);

            NotificationManagerCompat notificationCompat = NotificationManagerCompat.from(context);
            notificationCompat.notify(1, builder.build());
        }

    }

}