package it.bestclient.android;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class UserActivity extends AppCompatActivity {

    TextView email;
    TextView piva;
    Button logoutBtn;
    Button notificationBtn;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Context context;
    RadioGroup buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.settings);

        sp = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        editor = sp.edit();

        context = this;
        email = findViewById(R.id.emailProfile);
        piva = findViewById(R.id.pivaProfile);
        logoutBtn = findViewById(R.id.logout);
        notificationBtn = findViewById(R.id.notificationButton);

        email.setText(sp.getString("email", ""));
        piva.setText(sp.getString("piva", ""));

        logoutBtn.setOnClickListener(v -> {

            editor.putString("email", "");
            editor.putString("password", "");
            editor.putString("piva", "");
            editor.putString("uid", "");
            editor.apply();

            Intent intent = new Intent(UserActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        notificationBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            builder.setTitle(R.string.modalitaNotifica);
            View viewDialog = inflater.inflate(R.layout.radio_buttons, null);
            buttons = viewDialog.findViewById(R.id.radioButtonGroup);
            int checked = Integer.parseInt(sp.getString("notificationPreference", String.valueOf(R.id.notification)));
            buttons.check(checked);
            builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        int newchecked = buttons.getCheckedRadioButtonId();
                        editor.putString("notificationPreference", String.valueOf(newchecked));
                        editor.apply();
                        dialog.dismiss();
                        }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());
                AlertDialog dialog = builder.create();
                dialog.show();
        });
    }
}