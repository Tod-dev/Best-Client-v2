package com.example.progettobiancotodaro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class IncomingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        CallListener cl = new CallListener();
        tm.listen(cl, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class CallListener extends PhoneStateListener{
        public void onCallStateChanged(int state, String incomingNumber){
            String stato;

            switch(state){
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

            MainActivity.tv.setText(stato+" "+incomingNumber);
        }
    }
}
