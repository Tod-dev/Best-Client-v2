package com.example.progettobiancotodaro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class IncomingReceiver extends BroadcastReceiver {

    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        CallListener cl = new CallListener();
        tm.listen(cl, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class CallListener extends PhoneStateListener{

        public void onCallStateChanged(int state, String incomingNumber){
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
            if(incomingNumber.length()>0){
                String onlyNumber = incomingNumber.substring(incomingNumber.length()-10);
                String feedBack = "⭐";
                if(onlyNumber.equals("3312511781"))  feedBack = "⭐⭐⭐⭐⭐";
                Toast.makeText(context,onlyNumber+" "+feedBack, Toast.LENGTH_SHORT).show();
                //MainActivity.tv.setText(stato+" "+incomingNumber);
            }
        }
    }
}