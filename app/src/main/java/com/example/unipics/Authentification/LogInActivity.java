package com.example.unipics.Authentification;


import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.unipics.MainMenu.FolderActivity;
import com.example.unipics.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LogInActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private EditText mEmailField, mPasswordField;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }


    //if user is already logged in, skip this activity
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            startMainMenu();
        }

    }

    private void startMainMenu() {
        Intent intent = new Intent(LogInActivity.this, FolderActivity.class);
        startActivity(intent);
        //finish prevents user from going back to login screen
        finish();

    }


    private void init() {
        // Initialize Views
        mEmailField = findViewById(R.id.editText_email);
        mPasswordField = findViewById(R.id.editText_password);
        Button btnSignIn = findViewById(R.id.btn_signIn);
        TextView mSignUp = findViewById(R.id.textView_goToSignUp);
        mProgressBar = findViewById(R.id.progressBar_login);

        btnSignIn.setOnClickListener(this);
        mSignUp.setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }


    private void signIn(){
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if (email.isEmpty()){
            mEmailField.setError("Email is required");
            mEmailField.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailField.setError("Please enter a valid email");
            mEmailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mPasswordField.setError("Password is required");
            mPasswordField.requestFocus();
            return;
        }

        if (password.length() < 6) {
            mPasswordField.setError("Minimum lenght of password should be 6");
            mPasswordField.requestFocus();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, start MainActivity
                            startMainMenu();
                            Log.d(TAG, "signInWithEmail:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Authentication failed. Maybe wrong email or password.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_signIn:
                signIn();
                break;

            case R.id.textView_goToSignUp:
                startActivity(new Intent(LogInActivity.this, RegisterActivity.class));
                break;
        }
    }
}
