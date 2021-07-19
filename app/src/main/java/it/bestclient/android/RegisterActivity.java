package it.bestclient.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import it.bestclient.android.components.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    Button registerBtn;
    EditText email;
    EditText password;
    EditText piva;
    SharedPreferences sp;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        /*go back button in action bar*/
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        actionBar.setTitle(R.string.title_activity_register);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        piva = findViewById(R.id.piva);

        /*close keyboard if out of focus*/
        ConstraintLayout l = findViewById(R.id.containerRegister);
        l.setOnClickListener(v -> {
            View view = RegisterActivity.this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        registerBtn = findViewById(R.id.register);
        registerBtn.setOnClickListener(v -> registerUser());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void registerUser(){
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();
        String pivaText = piva.getText().toString();

        sp = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

        if(emailText.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            email.setError("Please provide a valid email");
            email.requestFocus();
            return;
        }
        if(passwordText.length() < 6){
            password.setError("Please provide a password, at least 6 characters");
            password.requestFocus();
            return;
        }
        if(pivaText.length() != 11 || !pivaText.matches("[0-9]+")){
            piva.setError("Please provide valid PIVA");
            piva.requestFocus();
            return;
        }

        User user = new User(emailText, pivaText);

        auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(uid).setValue(user);

                        @SuppressLint("CommitPrefEdits")
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("email", emailText);
                        editor.putString("password", passwordText);
                        editor.putString("piva", pivaText);
                        editor.putString("uid", uid);

                        editor.putString("filter", String.valueOf(HomeActivity.NO_FILTER));
                        editor.putString("scelta", String.valueOf(HomeActivity.CHIAMATE_ENTRATA));    //chiamate in entrata
                        editor.apply();

                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                    else{
                        try {
                            throw task.getException();
                        }catch(FirebaseAuthUserCollisionException e) {
                            email.setError("Email already in use");
                            email.requestFocus();
                        }catch(Exception e) {
                            Log.e("TAG", e.getMessage());
                        }
                    }
                });

    }
}