package com.example.unipics.Authentification;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.unipics.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private EditText mEmail, mPassword, mPasswordAgain;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        //initialize views
        mEmail = findViewById(R.id.editText_email);
        mPassword = findViewById(R.id.editText_password);
        mPasswordAgain = findViewById(R.id.editText_passwordAgain);
        Button btnRegister = findViewById(R.id.btn_register);
        TextView backToLogIn = findViewById(R.id.textView_backToLogin);
        mProgressBar = findViewById(R.id.progressBar_register);
        //set onClickListener
        backToLogIn.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        //initialize FirebaseAuth-Object
        mAuth = FirebaseAuth.getInstance();

    }

    private void signUpUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String passwordAgain = mPasswordAgain.getText().toString().trim();

        /*check conditions and set error messages in the following if clauses
         *first check if there is an email entered
         */
        if (email.isEmpty()) {
            //set error message
            mEmail.setError("E-Mail eingeben");
            //requests the focus to the view, where the error appears
            mEmail.requestFocus();
            return;
        }

        //checks if email address is a valid email adress
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Gib eine gültige E-Mail an");
            mEmail.requestFocus();
            return;
        }
        //checks if no password is entered
        if (password.isEmpty()) {
            mPassword.setError("Passwort eingeben");
            mPassword.requestFocus();
            return;
        }
        //checks if password is less then 6 characters
        if (password.length() < 6) {
            mPassword.setError("Passwort muss mindestens 6 Zeichen lang sein");
            mPassword.requestFocus();
            return;
        }
        //checks if entered passwords are equal
        if (!password.equals(passwordAgain)) {
            mPasswordAgain.setError("Passwörter sind nicht gleich");
            mPasswordAgain.requestFocus();
            return;
        }

        registerProcess(email, password);
    }

    private void registerProcess(String email, String password) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, go to login screen
                            goToLogin();
                            Log.d(TAG, "createUserWithEmail:success");

                        } else {
                            // If sign in fails, display a message to the user.
                            //if there is no internet connection...
                            if (!isNetworkAvailable()) {
                                Toast.makeText(RegisterActivity.this, "Kein Internet...", Toast.LENGTH_LONG).show();
                            } else {
                                //if any other error is the cause...
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Registrierung fehlgeschlagen. Hast du dich mit dieser E-Mail bereits registriert?",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                signUpUser();
                break;

            case R.id.textView_backToLogin:
                goToLogin();
        }
    }

    //go back to login activity
    private void goToLogin() {
        startActivity(new Intent(RegisterActivity.this, LogInActivity.class));
        finish();
    }

    //returns a boolean -> true if there is internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
