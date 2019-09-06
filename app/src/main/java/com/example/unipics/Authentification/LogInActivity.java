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
import com.example.unipics.MainMenu.FolderActivity;
import com.example.unipics.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.example.unipics.Constants.RC_SIGN_IN;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LogInActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private EditText mEmailField, mPasswordField;
    private ProgressBar mProgressBar;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {
        // Initialize Views
        mEmailField = findViewById(R.id.editText_email);
        mPasswordField = findViewById(R.id.editText_password);
        Button btnSignIn = findViewById(R.id.btn_signIn);
        TextView mSignUp = findViewById(R.id.textView_goToSignUp);
        mProgressBar = findViewById(R.id.progressBar_login);
        //set onClickListener
        btnSignIn.setOnClickListener(this);
        mSignUp.setOnClickListener(this);
        findViewById(R.id.sign_in_button_google).setOnClickListener(this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    //if user is already logged in, skip the login activity
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            startMainMenu();
        }

    }

    //starts the folder activity
    private void startMainMenu() {
        Intent intent = new Intent(LogInActivity.this, FolderActivity.class);
        startActivity(intent);
        //finish prevents user from going back to login screen
        finish();

    }

    //checking if networt is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //sign in to firebase
    private void signInWithEmailPassword(){
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        /*check conditions and set error messages in the following if clauses
         *checks if no email entered
         */
        if (email.isEmpty()){
            //set error message
            mEmailField.setError("E-Mail eingeben");
            //requests the focus to the view, where the error appears
            mEmailField.requestFocus();
            return;
        }

        //checks if email address is a valid email adress
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailField.setError("Gib eine gültige E-Mail an");
            mEmailField.requestFocus();
            return;
        }

        //checks if no password is entered
        if (password.isEmpty()) {
            mPasswordField.setError("Passwort eingeben");
            mPasswordField.requestFocus();
            return;
        }

        //checks if password is less then 6 characters
        if (password.length() < 6) {
            mPasswordField.setError("Passwort muss mindestens 6 Zeichen lang sein");
            mPasswordField.requestFocus();
            return;
        }

        signInProcessEmailPassword(email, password);
    }

    private void signInProcessEmailPassword(String email, String password) {
        mProgressBar.setVisibility(View.VISIBLE);
        //sign in method from firebase
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
                            /* If sign in fails, display a message to the user.
                             * first check if there is an internetconnection, if not display a message
                             */
                            if (!isNetworkAvailable()){
                                Toast.makeText(LogInActivity.this, "Kein Internet...", Toast.LENGTH_LONG).show();
                            }else{
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LogInActivity.this, "Anmelden fehlgeschlagen. Kontrolliere noch einmal deine E-Mail und dein Passwort",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_signIn:
                signInWithEmailPassword();
                break;

            case R.id.textView_goToSignUp:
                startActivity(new Intent(LogInActivity.this, RegisterActivity.class));
                break;

            case R.id.sign_in_button_google:
                signInWithGoogle();
        }
    }

    //starts intent to login with google
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!isNetworkAvailable()){
            Toast.makeText(LogInActivity.this, "Kein Internet...", Toast.LENGTH_LONG).show();
            return;
        }
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    //login to firebase with google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //set progress bar while login process
        mProgressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            startMainMenu();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Anmeldung fehlgeschlagen", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}
