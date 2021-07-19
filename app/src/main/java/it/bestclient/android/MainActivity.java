package it.bestclient.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_splash);

        /*HIDE ACTION BAR*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        /*SPLASH SCREEN 3 seconds*/
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, RatingActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}