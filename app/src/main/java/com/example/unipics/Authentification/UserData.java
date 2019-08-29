package com.example.unipics.Authentification;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.unipics.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserData extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        TextView tvUserData = findViewById(R.id.textView_data);
        if (user != null){
            tvUserData.setText("Name: " + user.getUid());
        }

        Button logout = findViewById(R.id.btn_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserData.this, LogInActivity.class));
            }
        });
    }
}
