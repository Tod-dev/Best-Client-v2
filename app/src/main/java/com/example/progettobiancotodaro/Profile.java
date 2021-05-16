package com.example.progettobiancotodaro;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.progettobiancotodaro.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Profile extends AppCompatActivity {

    SharedPreferences sp;
    TextView email;
    TextView piva;

    @SuppressLint({"SetTextI18n", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.profile);

        BottomNavigationView bn = findViewById(R.id.bottomMenu);
        bn.setSelectedItemId(R.id.profileBtn);
        bn.setOnNavigationItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.homeBtn:{
                    Intent intent = new Intent(Profile.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;
                }
                case R.id.settingsBtn:{
                    Intent intent = new Intent(Profile.this, SettingsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    break;
                }
                case R.id.profileBtn:{
                    break;
                }
                default: break;
            }

            return true;
        });

        sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        email = findViewById(R.id.emailProfile);
        piva = findViewById(R.id.pivaProfile);
        email.setText("Email: "+sp.getString("email", ""));
        piva.setText("Partita IVA: "+sp.getString("piva", ""));


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        menu.findItem(R.id.logout).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

            @SuppressLint("CommitPrefEdits")
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("email", "");
            editor.putString("password", "");
            editor.putString("piva", "");
            editor.putString("uid", "");
            editor.apply();

            Intent intent = new Intent(Profile.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
}