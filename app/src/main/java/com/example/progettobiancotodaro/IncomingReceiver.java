package com.example.progettobiancotodaro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Objects;

public class IncomingReceiver extends BroadcastReceiver {
    //final String OUR_ACTION = "android.intent.action.PHONE_STATE";
    Context context;
   // private NotificationManagerCompat notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        Log.d("myTAG",intent.getAction()); //android.intent.action.PHONE_STATE
        switch (intent.getAction()) {
            case OUR_ACTION:
                this.context = context;
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                CallListener cl = new CallListener();
                tm.listen(cl, PhoneStateListener.LISTEN_CALL_STATE);
                break;
            default:
                break;
        }
                if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            showToast(context, "Call started...");
        } else

         */


        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            this.context = context;
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            CallListener cl = new CallListener();
            tm.listen(cl, PhoneStateListener.LISTEN_CALL_STATE);

        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Log.d("CALL: ", "CALL ENDED");
        }
    }

    private class CallListener extends PhoneStateListener{

        public void onCallStateChanged(int state, String incomingNumber){

            if (incomingNumber.length() > 0) {
                String onlyNumber = incomingNumber.substring(incomingNumber.length() - 10);
                String feedBack = "⭐";
                if (onlyNumber.equals("3312511781")) feedBack = "⭐⭐⭐⭐⭐";
            //String stato;
             /*      switch(state){
                case TelephonyManager.CALL_STATE_IDLE: {
                    stato = "The phone is idle";
                    break;
                }
                case TelephonyManager.CALL_STATE_OFFHOOK: {
                    stato = "The phone is in use";
                    break;
                }
                case TelephonyManager.CALL_STATE_RINGING:{
                    stato = "The phone is ringing";
                    break;
                }
                default: stato="null";
                    break;
            }
               */
                Log.d("STATE: ", Integer.toString(state));
                Toast toast = Toast.makeText(context, onlyNumber + " " + feedBack, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show(); /* DURA DUE SQUILLI CIRCA -> TROPPO POCO!!!*/

            }


        }


    }

}