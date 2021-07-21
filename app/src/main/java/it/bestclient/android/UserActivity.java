package it.bestclient.android;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserActivity extends AppCompatActivity {

    TextView email;
    TextView piva;
    Button logoutBtn;
    Button notificationBtn;
    Button deleteUser;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Context context;
    RadioGroup buttons;

    @RequiresApi(api = Build.VERSION_CODES.O)
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
        deleteUser = findViewById(R.id.deleteUser);

        email.setText(sp.getString("email", ""));
        piva.setText(sp.getString("piva", ""));

        logoutBtn.setOnClickListener(v -> {

            Utils.resetPreferences(editor, true);

            Intent intent = new Intent(UserActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.to_left_in, R.anim.to_right_out);
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

        deleteUser.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            builder.setTitle(R.string.deleteUser);
            View viewDialog = inflater.inflate(R.layout.delete_popup, null);
            builder.setView(viewDialog)
                    .setPositiveButton(R.string.positiveButton, (dialog, which) -> {
                        String uid = sp.getString("uid", "");
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference reference = database.getReference("Users").child(uid);

                        reference.removeValue();

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Utente eliminato correttamente", Toast.LENGTH_LONG).show();
                                        Utils.resetPreferences(editor, true);
                                        Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.to_left_in, R.anim.to_right_out);
                                    }
                                    else {
                                        Toast.makeText(context, "Errore nell'eliminazione dell'utente", Toast.LENGTH_LONG).show();
                                    }
                                });

                        dialog.dismiss();
                    }).setNegativeButton(R.string.negativeButton, (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.to_right_in, R.anim.to_left_out);
    }
}