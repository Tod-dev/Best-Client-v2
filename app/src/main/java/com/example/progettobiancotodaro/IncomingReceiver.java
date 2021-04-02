package com.example.progettobiancotodaro;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

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

                String onlyNumber = incomingNumber.substring(incomingNumber.length() - 10);

                float rating = myDBhelper.getRating(onlyNumber);
                if(rating == -1){
                    Toast toast = Toast.makeText(context, "Number not rated", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
                else{
                    if(rating == 0){
                        Toast toast = Toast.makeText(context, onlyNumber+": 0 stars!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                    }
                    else{
                        int roundRating = Math.round(rating);
                        StringBuilder feedBack = new StringBuilder();

                        for(int i = 0; i < roundRating; i++){
                            feedBack.append("⭐");
                        }

                        Log.d("STATE: ", Integer.toString(state));
                        Toast toast = Toast.makeText(context, onlyNumber + " " + feedBack, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show(); /* DURA DUE SQUILLI CIRCA -> TROPPO POCO!!!*/
                    }

                }


            }


        }


    }

}