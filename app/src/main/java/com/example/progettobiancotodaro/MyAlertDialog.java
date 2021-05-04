package com.example.progettobiancotodaro;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MyAlertDialog extends Activity {
    AlertDialog dialog;
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void makeDialog(String title, String text){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title);
            builder.setMessage(text)
                    .setPositiveButton("ok", (dialog, which) -> {
                        //Toast.makeText(this,"DIALOGO NASCOSTO",Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
            dialog = builder.create();
            dialog.show();
            TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            dialog.setCanceledOnTouchOutside(false);
    }

    //Per evitare di creare troppe istanze - se chiamate ravvicinate
    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( dialog!=null && dialog.isShowing() ){
            dialog.cancel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //hide activity title
        setContentView(R.layout.rating_avg);
        Intent intent = getIntent();
        String title = intent.getExtras().getString("title");
        String text = intent.getExtras().getString("text");
        makeDialog(title,text);
    }
}