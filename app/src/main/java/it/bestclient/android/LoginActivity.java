package it.bestclient.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@RequiresApi(api = Build.VERSION_CODES.O)
public class LoginActivity extends AppCompatActivity {
    String[] Permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS};
    Button loginBtn;
    EditText email;
    EditText password;
    TextView noAccount;
    SharedPreferences sp;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*Request Permissions*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) +
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, Permissions, 1);
        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        sp = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

        noAccount = findViewById(R.id.NoAccount);
        noAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.to_right_in, R.anim.to_left_out);
        });

        /*click outside keyboard close it*/
        ConstraintLayout l = findViewById(R.id.containerLogin);
        l.setOnClickListener(v -> {
            View view = LoginActivity.this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        loginBtn = findViewById(R.id.login);
        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser(){
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();

        if(emailText.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            email.setError("Please provide a valid email");
            email.requestFocus();
            return;
        }
        if(passwordText.isEmpty()){
            password.setError("Please provide a password");
            password.requestFocus();
            return;
        }

        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                @SuppressLint("CommitPrefEdits")
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("email", emailText);
                editor.putString("password", passwordText);
                getPiva(auth.getUid());
                editor.putString("uid", auth.getUid());

                Utils.resetPreferences(editor, false);

                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.to_right_in, R.anim.to_left_out);
                //Toast.makeText(LoginActivity.this, "Login", Toast.LENGTH_LONG).show();
            /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);*/
            }
            else Toast.makeText(LoginActivity.this, "Email o password errati", Toast.LENGTH_LONG).show();
        });

    }

    public void getPiva(String uid){
        /*set piva in preferences from user id setted*/
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("Users").child(uid).child("piva");

        ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String piva = dataSnapshot.getValue(String.class);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("piva", piva);
                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Main", "Failed to read value.", error.toException());
            }
        });
    }
}