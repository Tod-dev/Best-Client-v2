package it.bestclient.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import it.bestclient.android.RatingModel.Rating;
import it.bestclient.android.RatingModel.RatingAVGOnDB;
import it.bestclient.android.RatingModel.RatingBigOnDB;
import it.bestclient.android.components.Contact;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static it.bestclient.android.Utils.USERS;
import static it.bestclient.android.Utils.VALUTAZIONI;
import static it.bestclient.android.Utils.displayRatingStars;
import static it.bestclient.android.Utils.filterOnlyDigits;

public class IncomingReceiver extends BroadcastReceiver {
    Context context;
    RatingAVGOnDB curRating;
    SharedPreferences sp;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                /*THE PHONE IS RINGING*/
                this.context = context;
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                CallListener cl = new CallListener();
                tm.listen(cl, PhoneStateListener.LISTEN_CALL_STATE);

            } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d("CALL: ", "CALL ENDED");
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
        public void makeToast(Rating r){
            String message = r.getNumero() +"\n";
            if(r.getVoto() == -1){
                /* RATING NON INSERITO DALL'UTENTE */
                //message += "Numero non valutato da te\n";
                if(r.getVoto_medio() == -1){
                    //message = "Numero non valutato!";
                    return;
                }
                else{
                    message += "\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66   "+displayRatingStars(r.getVoto_medio());
                }
            }else{
                message += "\uD83E\uDDD1   "+displayRatingStars(r.getVoto());
                //message += "data: "+ratingToShow.getDate()+"\n";
                if(r.getVoto_medio() != -1){
                    message += "\n\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66   "+displayRatingStars(r.getVoto_medio());
                }
                //message += r.getCommento().isEmpty() ? "" : "\n\uD83D\uDCE3   "+r.getCommento();
            }

            Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }

        /* DISPLAY OF NOTIFICATION */
        public void makeNotification(Rating r){
            String message = "";
            if(r.getVoto() == -1){
                /* RATING NON INSERITO DALL'UTENTE */
                //message += "Non valutato da te,";
                if(r.getVoto_medio() == -1){
                    //message = "Numero non valutato!";
                    return;
                }
                else{
                    message += "\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66 "+displayRatingStars(r.getVoto_medio());
                }
            }else{
                message += "\uD83E\uDDD1 "+displayRatingStars(r.getVoto())+"\n";
                //message += "data: "+ratingToShow.getDate()+"\n";
                if (r.getVoto_medio() != -1) {
                    message += "    \uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66 "+displayRatingStars(r.getVoto_medio());
                }

            }



            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel("Rating Notification", "Rating Notification", NotificationManager.IMPORTANCE_HIGH);
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Rating Notification");
            builder.setContentTitle(r.getNumero());
            builder.setContentText(message);
            builder.setSmallIcon(R.drawable.ic_baseline_star_24);
            builder.setColorized(true);
            builder.setPriority(Notification.PRIORITY_MAX);

            Intent intent = new Intent(context, RatingActivity.class);
            intent.putExtra(RatingActivity.NUMBER,r.getNumero());
            intent.putExtra(RatingActivity.VOTO,r.getVoto());
            intent.putExtra(RatingActivity.COMMENT,r.getCommento());
            intent.putExtra(RatingActivity.MEDIO,r.getVoto_medio());

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(contentIntent);

            NotificationManagerCompat notificationCompat = NotificationManagerCompat.from(context);
            notificationCompat.notify(1, builder.build());
        }

        /* DISPLAY OF POPUP */
        public void makePopup(String title, Rating  r){
            String message = "";
            if(r.getVoto() == -1){
                /* RATING NON INSERITO DALL'UTENTE */
                //message += "Numero non valutato da te\n";
                if(r.getVoto_medio() == -1){
                    //message = "Numero non valutato!";
                    return;
                }
                else{
                    message += "\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66   "+displayRatingStars(r.getVoto_medio());
                }
            }else{
                message += "\uD83E\uDDD1   "+displayRatingStars(r.getVoto());
                //message += "data: "+ratingToShow.getDate()+"\n";
                if (r.getVoto_medio() != -1) {
                    message += "\n\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66   " + displayRatingStars(r.getVoto_medio());
                }
                //message += r.getCommento().isEmpty() ? "" : "\n\uD83D\uDCE3   "+r.getCommento()+"\n";
            }

            Intent intent = new Intent(context, PopUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("title", title);
            intent.putExtra("text", message);
            context.startActivity(intent);
        }

        /*Selects the object from firebase db using phone number and copies it into curRating object -> than send a notification (setting switch the notification type) */
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void getRatingFromNumber(String number){
            String onlyNumber = number;

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

                    Rating ratingToShow = new Rating();
                    ratingToShow.setNumero(finalNumber);

                    /*LETTURA DEL VOTO MEDIO*/

                    if(dataSnapshot.getValue() == null){
                        ratingToShow.setVoto_medio(-1);
                    }
                    else{
                        curRating = dataSnapshot.getValue(RatingAVGOnDB.class);
                        double rating = curRating.getVotoMedio();
                        ratingToShow.setVoto_medio(rating);
                    }


                    /* LETTURA DEL RATING BIG */
                    String uid = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE).getString("uid", "");

                    // path
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = db.getReference(USERS).child(uid).child(VALUTAZIONI).child(onlyNumber);


                    // Read from the database
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int preference = Integer.parseInt(sp.getString("notificationPreference", String.valueOf(R.id.notification)));

                            RatingBigOnDB r = dataSnapshot.getValue(RatingBigOnDB.class);
                            if(dataSnapshot.getValue() == null){
                                ratingToShow.setVoto(-1);
                            }
                            else{
                                ratingToShow.setVoto(r.getVoto());
                                ratingToShow.setDate(r.getDate());
                                ratingToShow.setCommento(r.getCommento());

                            }


                            switch (preference) {
                                case R.id.toast:
                                    makeToast(ratingToShow);
                                    break;
                                case R.id.notification:
                                    makeNotification(ratingToShow);
                                    break;
                                case R.id.popup:
                                    makePopup(finalNumber, ratingToShow);
                                    break;
                                default:
                                    makeNotification(ratingToShow);
                                    break;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w("ERROR", "Failed to read value.", error.toException());
                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.w("Main", "Failed to read value.", error.toException());
                }
            });
        }
    }
}