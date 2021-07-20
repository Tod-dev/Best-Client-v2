package it.bestclient.android;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UserActivity extends AppCompatActivity {

    TextView email;
    TextView piva;
    Button logoutBtn;
    Button notificationBtn;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.settings);

        sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

        email = findViewById(R.id.emailProfile);
        piva = findViewById(R.id.pivaProfile);
        logoutBtn = findViewById(R.id.logout);
        notificationBtn = findViewById(R.id.notificationButton);

        email.setText(sp.getString("email", ""));
        piva.setText(sp.getString("piva", ""));

        logoutBtn.setOnClickListener(v -> {
            @SuppressLint("CommitPrefEdits")
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("email", "");
            editor.putString("password", "");
            editor.putString("piva", "");
            editor.putString("uid", "");
            editor.apply();

            Intent intent = new Intent(UserActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}