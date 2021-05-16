package  com.example.progettobiancotodaro.ui.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.progettobiancotodaro.HomeActivity;
import com.example.progettobiancotodaro.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    int RC_SIGN_IN = 0;
    GoogleSignInClient mGoogleSignInClient;
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

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        sp = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

        noAccount = findViewById(R.id.NoAccount);
        noAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

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
        /*GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        com.google.android.gms.common.SignInButton googleLogin = (com.google.android.gms.common.SignInButton) findViewById(R.id.loginGoogle);
        googleLogin.setOnClickListener(v -> {
            if (v.getId() == R.id.loginGoogle) {
                signIn();
                // ...
            }
        });*/
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
                editor.apply();

                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                //Toast.makeText(LoginActivity.this, "Login", Toast.LENGTH_LONG).show();
            /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);*/
            }
            else Toast.makeText(LoginActivity.this, "Email o password errati", Toast.LENGTH_LONG).show();
        });

    }

    public void getPiva(String uid){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ratingsRef = database.getReference("Users").child(uid).child("piva");
        final String[] piva = new String[1];

        ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                piva[0] = dataSnapshot.getValue(String.class);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("piva", piva[0]);
                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("Main", "Failed to read value.", error.toException());
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            //Toast.makeText(this,"ok", Toast.LENGTH_SHORT).show();
            // Signed in successfully, show authenticated UI.
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }
}